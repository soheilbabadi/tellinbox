import { IsString, IsNotEmpty, IsOptional, IsEnum, IsBoolean, IsNumber, Min, Max } from 'class-validator';
import { EvaluatorRelationship } from '../enums';

export class CreateEvaluationCycleDto {
  @IsString()
  @IsNotEmpty()
  title: string;

  @IsString()
  @IsOptional()
  description?: string;

  @IsString()
  @IsNotEmpty()
  organizationId: string;

  @IsOptional()
  startDate?: Date;

  @IsOptional()
  endDate?: Date;

  @IsBoolean()
  @IsOptional()
  allowSelfEvaluation?: boolean = true;

  @IsBoolean()
  @IsOptional()
  allowPeerEvaluation?: boolean = true;

  @IsBoolean()
  @IsOptional()
  allowManagerEvaluation?: boolean = true;

  @IsBoolean()
  @IsOptional()
  allowSubordinateEvaluation?: boolean = true;

  @IsBoolean()
  @IsOptional()
  allowCustomerEvaluation?: boolean = false;

  @IsNumber()
  @Min(1)
  @Max(720)
  @IsOptional()
  inviteValidityHours?: number = 48;

  @IsBoolean()
  @IsOptional()
  isAnonymous?: boolean = true;

  @IsOptional()
  evaluationCriteria?: Record<string, any>;
}

export class AddEvaluateeDto {
  @IsString()
  @IsNotEmpty()
  evaluateeId: string;

  @IsEnum(EvaluatorRelationship)
  relationship: EvaluatorRelationship;

  @IsString()
  @IsOptional()
  evaluatorId?: string;

  @IsString()
  @IsOptional()
  customMessage?: string;
}

export class SubmitEvaluationDto {
  @IsString()
  @IsNotEmpty()
  invitationId: string;

  @IsNotEmpty()
  answers: Record<string, any>;

  @IsString()
  @IsOptional()
  comments?: string;
}

export class EvaluationResultDto {
  evaluateeId: string;
  evaluateeName: string;
  relationship: EvaluatorRelationship;
  averageScore: number;
  totalResponses: number;
  completedAt: Date;
  breakdownByRelationship: Record<EvaluatorRelationship, { averageScore: number; count: number }>;
}

export class DashboardSummaryDto {
  cycleId: string;
  cycleTitle: string;
  status: string;
  totalInvitees: number;
  completedEvaluations: number;
  pendingEvaluations: number;
  expirationRate: number;
  averageOverallScore: number;
  resultsByRelationship: Record<EvaluatorRelationship, { averageScore: number; count: number }>;
  resultsByEvaluatee: EvaluationResultDto[];
}

export class UserEvaluationResultsDto {
  cycleId: string;
  cycleTitle: string;
  averageScore: number;
  totalResponses: number;
  completedAt: Date;
  breakdownByRelationship: Record<EvaluatorRelationship, { averageScore: number; count: number }>;
  comments: Array<{ comment: string; relationship: EvaluatorRelationship; createdAt: Date }>;
}
