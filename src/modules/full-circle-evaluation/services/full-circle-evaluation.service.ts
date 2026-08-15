import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThan, MoreThan } from 'typeorm';
import { EvaluationCycle } from './entities/evaluation-cycle.entity';
import { EvaluationInvitation } from './entities/evaluation-invitation.entity';
import { EvaluationResponse } from './entities/evaluation-response.entity';
import { CreateEvaluationCycleDto, AddEvaluateeDto, SubmitEvaluationDto, DashboardSummaryDto, EvaluationResultDto, UserEvaluationResultsDto } from './dto';
import { EvaluationCycleStatus, EvaluatorRelationship, InvitationStatus } from './enums';
import { v4 as uuidv4 } from 'uuid';
import { ResourceNotFoundException, BadRequestException, NotFoundException } from '../../common/exception';

@Injectable()
export class Evaluation360Service {
  constructor(
    @InjectRepository(EvaluationCycle)
    private readonly cycleRepo: Repository<EvaluationCycle>,
    @InjectRepository(EvaluationInvitation)
    private readonly invitationRepo: Repository<EvaluationInvitation>,
    @InjectRepository(EvaluationResponse)
    private readonly responseRepo: Repository<EvaluationResponse>,
  ) {}

  async createCycle(dto: CreateEvaluationCycleDto): Promise<EvaluationCycle> {
    const cycle = this.cycleRepo.create({
      ...dto,
      status: EvaluationCycleStatus.DRAFT,
      startDate: dto.startDate || new Date(),
      endDate: dto.endDate || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      inviteValidityHours: dto.inviteValidityHours || 48,
      isAnonymous: dto.isAnonymous !== false,
    });
    return await this.cycleRepo.save(cycle);
  }

  async activateCycle(cycleId: string): Promise<EvaluationCycle> {
    const cycle = await this.cycleRepo.findOne({ where: { id: cycleId } });
    if (!cycle) throw new ResourceNotFoundException('چرخه ارزیابی یافت نشد');
    
    if (cycle.status !== EvaluationCycleStatus.DRAFT) {
      throw new BadRequestException('چرخه باید در وضعیت پیش‌نویس باشد');
    }

    cycle.status = EvaluationCycleStatus.ACTIVE;
    return await this.cycleRepo.save(cycle);
  }

  async addEvaluatee(cycleId: string, dto: AddEvaluateeDto): Promise<EvaluationInvitation> {
    const cycle = await this.cycleRepo.findOne({ 
      where: { id: cycleId },
      relations: ['invitations']
    });
    if (!cycle) throw new ResourceNotFoundException('چرخه ارزیابی یافت نشد');
    if (cycle.status !== EvaluationCycleStatus.ACTIVE) {
      throw new BadRequestException('چرخه باید فعال باشد');
    }

    const expiresAt = new Date();
    expiresAt.setHours(expiresAt.getHours() + cycle.inviteValidityHours);

    const inviteToken = uuidv4();
    
    const invitation = this.invitationRepo.create({
      cycleId: cycle.id,
      evaluatorId: dto.evaluatorId || null,
      evaluateeId: dto.evaluateeId,
      relationship: dto.relationship,
      status: InvitationStatus.PENDING,
      inviteToken,
      expiresAt,
      customMessage: dto.customMessage,
    });

    return await this.invitationRepo.save(invitation);
  }

  async getInviteByToken(token: string): Promise<EvaluationInvitation | null> {
    return await this.invitationRepo.findOne({
      where: { 
        inviteToken: token,
        status: InvitationStatus.PENDING,
        expiresAt: MoreThan(new Date())
      },
      relations: ['cycle', 'evaluator', 'evaluatee'],
    });
  }

  async acceptInvitation(invitationId: string): Promise<EvaluationInvitation> {
    const invitation = await this.invitationRepo.findOne({ where: { id: invitationId } });
    if (!invitation) throw new ResourceNotFoundException('دعوتنامه یافت نشد');
    
    if (invitation.status !== InvitationStatus.PENDING) {
      throw new BadRequestException('وضعیت دعوتنامه نامعتبر است');
    }

    invitation.status = InvitationStatus.ACCEPTED;
    invitation.acceptedAt = new Date();
    return await this.invitationRepo.save(invitation);
  }

  async submitEvaluation(dto: SubmitEvaluationDto): Promise<EvaluationResponse> {
    const invitation = await this.invitationRepo.findOne({
      where: { id: dto.invitationId },
      relations: ['cycle'],
    });
    if (!invitation) throw new NotFoundException('دعوتنامه یافت نشد');
    
    if (invitation.status === InvitationStatus.EXPIRED) {
      throw new BadRequestException('مهلت دعوتنامه به پایان رسیده است');
    }

    let totalScore: number | null = null;
    if (dto.answers && typeof dto.answers === 'object') {
      const scores = Object.values(dto.answers).filter(v => typeof v === 'number');
      if (scores.length > 0) {
        totalScore = Math.round(scores.reduce((a, b) => a + b, 0) / scores.length);
      }
    }

    const response = this.responseRepo.create({
      invitationId: dto.invitationId,
      cycleId: invitation.cycleId,
      answers: dto.answers,
      comments: dto.comments,
      totalScore,
      isSubmitted: true,
      submittedAt: new Date(),
    });

    const savedResponse = await this.responseRepo.save(response);

    invitation.status = InvitationStatus.COMPLETED;
    invitation.completedAt = new Date();
    await this.invitationRepo.save(invitation);

    return savedResponse;
  }

  async getDashboardSummary(cycleId: string): Promise<DashboardSummaryDto> {
    const cycle = await this.cycleRepo.findOne({
      where: { id: cycleId },
      relations: ['invitations', 'responses'],
    });
    if (!cycle) throw new NotFoundException('چرخه ارزیابی یافت نشد');

    const invitations = await this.invitationRepo.find({
      where: { cycleId: cycle.id },
      relations: ['evaluatee'],
    });

    const responses = await this.responseRepo.find({
      where: { cycleId: cycle.id, isSubmitted: true },
      relations: ['invitation'],
    });

    const completedCount = invitations.filter(i => i.status === InvitationStatus.COMPLETED).length;
    const pendingCount = invitations.filter(i => i.status === InvitationStatus.PENDING).length;
    const expiredCount = invitations.filter(i => i.status === InvitationStatus.EXPIRED).length;

    const resultsByRelationship: Record<EvaluatorRelationship, { averageScore: number; count: number }> = {
      [EvaluatorRelationship.SELF]: { averageScore: 0, count: 0 },
      [EvaluatorRelationship.MANAGER]: { averageScore: 0, count: 0 },
      [EvaluatorRelationship.PEER]: { averageScore: 0, count: 0 },
      [EvaluatorRelationship.SUBORDINATE]: { averageScore: 0, count: 0 },
      [EvaluatorRelationship.CUSTOMER_INTERNAL]: { averageScore: 0, count: 0 },
      [EvaluatorRelationship.CUSTOMER_EXTERNAL]: { averageScore: 0, count: 0 },
    };

    responses.forEach(r => {
      const rel = r.invitation.relationship;
      if (r.totalScore !== null) {
        resultsByRelationship[rel].averageScore = 
          (resultsByRelationship[rel].averageScore * resultsByRelationship[rel].count + r.totalScore) / 
          (resultsByRelationship[rel].count + 1);
        resultsByRelationship[rel].count++;
      }
    });

    const overallScores = responses.filter(r => r.totalScore !== null).map(r => r.totalScore);
    const averageOverallScore = overallScores.length > 0 
      ? Math.round(overallScores.reduce((a, b) => a + b, 0) / overallScores.length) 
      : 0;

    const resultsByEvaluatee: EvaluationResultDto[] = [];
    const evaluateeMap = new Map<string, { responses: any[], name: string }>();

    invitations.forEach(inv => {
      if (!evaluateeMap.has(inv.evaluateeId)) {
        evaluateeMap.set(inv.evaluateeId, { responses: [], name: inv.evaluatee?.fullName || 'Unknown' });
      }
    });

    for (const [evaluateeId, data] of evaluateeMap.entries()) {
      const evalResponses = responses.filter(r => 
        invitations.find(i => i.id === r.invitationId)?.evaluateeId === evaluateeId
      );

      if (evalResponses.length > 0) {
        const scores = evalResponses.filter(r => r.totalScore !== null).map(r => r.totalScore);
        const avgScore = scores.length > 0 ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0;
        
        const breakdown: Record<EvaluatorRelationship, { averageScore: number; count: number }> = {
          [EvaluatorRelationship.SELF]: { averageScore: 0, count: 0 },
          [EvaluatorRelationship.MANAGER]: { averageScore: 0, count: 0 },
          [EvaluatorRelationship.PEER]: { averageScore: 0, count: 0 },
          [EvaluatorRelationship.SUBORDINATE]: { averageScore: 0, count: 0 },
          [EvaluatorRelationship.CUSTOMER_INTERNAL]: { averageScore: 0, count: 0 },
          [EvaluatorRelationship.CUSTOMER_EXTERNAL]: { averageScore: 0, count: 0 },
        };

        evalResponses.forEach(r => {
          const inv = invitations.find(i => i.id === r.invitationId);
          if (inv && r.totalScore !== null) {
            breakdown[inv.relationship].averageScore = 
              (breakdown[inv.relationship].averageScore * breakdown[inv.relationship].count + r.totalScore) / 
              (breakdown[inv.relationship].count + 1);
            breakdown[inv.relationship].count++;
          }
        });

        resultsByEvaluatee.push({
          evaluateeId,
          evaluateeName: data.name,
          relationship: EvaluatorRelationship.PEER,
          averageScore: avgScore,
          totalResponses: evalResponses.length,
          completedAt: evalResponses[evalResponses.length - 1].createdAt,
          breakdownByRelationship: breakdown,
        });
      }
    }

    return {
      cycleId: cycle.id,
      cycleTitle: cycle.title,
      status: cycle.status,
      totalInvitees: invitations.length,
      completedEvaluations: completedCount,
      pendingEvaluations: pendingCount,
      expirationRate: invitations.length > 0 ? Math.round((expiredCount / invitations.length) * 100) : 0,
      averageOverallScore: averageOverallScore,
      resultsByRelationship,
      resultsByEvaluatee,
    };
  }

  async getUserCompletedEvaluations(userId: string): Promise<EvaluationInvitation[]> {
    return await this.invitationRepo.find({
      where: { 
        evaluatorId: userId,
        status: InvitationStatus.COMPLETED,
      },
      relations: ['cycle', 'evaluatee'],
      order: { completedAt: 'DESC' },
    });
  }

  async getUserPendingEvaluations(userId: string): Promise<EvaluationInvitation[]> {
    return await this.invitationRepo.find({
      where: { 
        evaluatorId: userId,
        status: InvitationStatus.PENDING,
        expiresAt: MoreThan(new Date()),
      },
      relations: ['cycle', 'evaluatee'],
      order: { createdAt: 'DESC' },
    });
  }

  async expireOverdueInvitations(): Promise<void> {
    await this.invitationRepo.update(
      {
        status: InvitationStatus.PENDING,
        expiresAt: LessThan(new Date()),
      },
      { status: InvitationStatus.EXPIRED },
    );
  }

  async getUserEvaluationResults(userId: string): Promise<UserEvaluationResultsDto[]> {
    const invitations = await this.invitationRepo.find({
      where: { 
        evaluateeId: userId,
      },
      relations: ['cycle'],
    });

    if (invitations.length === 0) {
      return [];
    }

    const cycleIds = [...new Set(invitations.map(i => i.cycleId))];
    const results: UserEvaluationResultsDto[] = [];

    for (const cycleId of cycleIds) {
      const cycleInvitations = invitations.filter(i => i.cycleId === cycleId);
      
      const responses = await this.responseRepo.find({
        where: { 
          cycleId,
          isSubmitted: true,
        },
        relations: ['invitation'],
      });

      const relevantResponses = responses.filter(r => 
        cycleInvitations.some(inv => inv.id === r.invitationId)
      );

      if (relevantResponses.length === 0) {
        continue;
      }

      const scores = relevantResponses.filter(r => r.totalScore !== null).map(r => r.totalScore);
      const averageScore = scores.length > 0 
        ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) 
        : 0;

      const breakdownByRelationship: Record<EvaluatorRelationship, { averageScore: number; count: number }> = {
        [EvaluatorRelationship.SELF]: { averageScore: 0, count: 0 },
        [EvaluatorRelationship.MANAGER]: { averageScore: 0, count: 0 },
        [EvaluatorRelationship.PEER]: { averageScore: 0, count: 0 },
        [EvaluatorRelationship.SUBORDINATE]: { averageScore: 0, count: 0 },
        [EvaluatorRelationship.CUSTOMER_INTERNAL]: { averageScore: 0, count: 0 },
        [EvaluatorRelationship.CUSTOMER_EXTERNAL]: { averageScore: 0, count: 0 },
      };

      relevantResponses.forEach(r => {
        const inv = cycleInvitations.find(i => i.id === r.invitationId);
        if (inv && r.totalScore !== null) {
          breakdownByRelationship[inv.relationship].averageScore = 
            (breakdownByRelationship[inv.relationship].averageScore * breakdownByRelationship[inv.relationship].count + r.totalScore) / 
            (breakdownByRelationship[inv.relationship].count + 1);
          breakdownByRelationship[inv.relationship].count++;
        }
      });

      const comments = relevantResponses
        .filter(r => r.comments && r.comments.trim() !== '')
        .map(r => ({
          comment: r.comments,
          relationship: cycleInvitations.find(i => i.id === r.invitationId)?.relationship || EvaluatorRelationship.PEER,
          createdAt: r.createdAt,
        }));

      const cycle = await this.cycleRepo.findOne({ where: { id: cycleId } });

      results.push({
        cycleId,
        cycleTitle: cycle?.title || 'Unknown Cycle',
        averageScore,
        totalResponses: relevantResponses.length,
        completedAt: relevantResponses[relevantResponses.length - 1].createdAt,
        breakdownByRelationship,
        comments,
      });
    }

    return results;
  }
}
