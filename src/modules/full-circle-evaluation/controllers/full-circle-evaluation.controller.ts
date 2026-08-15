import { Controller, Get, Post, Put, Body, Param, Query, UseGuards, Request } from '@nestjs/common';
import { Evaluation360Service } from '../services/full-circle-evaluation.service';
import { CreateEvaluationCycleDto, AddEvaluateeDto, SubmitEvaluationDto } from '../dto';
import { JwtAuthGuard } from '../../auth/guards/jwt-auth.guard';
import { OrganizationAdminGuard } from '../../auth/guards/organization-admin.guard';

@Controller('api/evaluations/360')
export class Evaluation360Controller {
  constructor(private readonly evaluationService: Evaluation360Service) {}

  @Post()
  @UseGuards(JwtAuthGuard, OrganizationAdminGuard)
  async createCycle(@Body() dto: CreateEvaluationCycleDto, @Request() req) {
    dto.organizationId = req.user.organizationId;
    return this.evaluationService.createCycle(dto);
  }

  @Put(':id/activate')
  @UseGuards(JwtAuthGuard, OrganizationAdminGuard)
  async activateCycle(@Param('id') id: string) {
    return this.evaluationService.activateCycle(id);
  }

  @Post(':id/evaluatees')
  @UseGuards(JwtAuthGuard, OrganizationAdminGuard)
  async addEvaluatee(@Param('id') cycleId: string, @Body() dto: AddEvaluateeDto) {
    return this.evaluationService.addEvaluatee(cycleId, dto);
  }

  @Get('invite/:token')
  async getInviteByToken(@Param('token') token: string) {
    const invitation = await this.evaluationService.getInviteByToken(token);
    if (!invitation) {
      return { valid: false, message: 'دعوتنامه نامعتبر یا منقضی شده است' };
    }
    return {
      valid: true,
      invitation: {
        id: invitation.id,
        cycleTitle: invitation.cycle.title,
        evaluatorName: invitation.evaluator?.fullName,
        evaluateeName: invitation.evaluatee.fullName,
        relationship: invitation.relationship,
        expiresAt: invitation.expiresAt,
      },
    };
  }

  @Post('invite/:id/accept')
  async acceptInvitation(@Param('id') id: string) {
    return this.evaluationService.acceptInvitation(id);
  }

  @Post('submit')
  async submitEvaluation(@Body() dto: SubmitEvaluationDto) {
    return this.evaluationService.submitEvaluation(dto);
  }

  @Get(':id/dashboard')
  @UseGuards(JwtAuthGuard, OrganizationAdminGuard)
  async getDashboard(@Param('id') cycleId: string) {
    return this.evaluationService.getDashboardSummary(cycleId);
  }

  @Get('user/completed')
  @UseGuards(JwtAuthGuard)
  async getUserCompleted(@Request() req) {
    return this.evaluationService.getUserCompletedEvaluations(req.user.id);
  }

  @Get('user/pending')
  @UseGuards(JwtAuthGuard)
  async getUserPending(@Request() req) {
    return this.evaluationService.getUserPendingEvaluations(req.user.id);
  }

  @Get('user/results')
  @UseGuards(JwtAuthGuard)
  async getUserResults(@Request() req) {
    return this.evaluationService.getUserEvaluationResults(req.user.id);
  }
}
