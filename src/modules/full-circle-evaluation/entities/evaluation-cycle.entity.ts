import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  OneToMany,
  JoinColumn,
} from 'typeorm';
import { Organization } from '../../organization/entities/organization.entity';
import { User } from '../../user/entities/user.entity';
import { EvaluationCycleStatus } from '../enums/evaluation-cycle-status.enum';

@Entity('evaluation_cycle')
export class EvaluationCycle {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  title: string;

  @Column({ type: 'text', nullable: true })
  description: string;

  @ManyToOne(() => Organization)
  @JoinColumn({ name: 'organization_id' })
  organization: Organization;

  @Column({ type: 'uuid' })
  organizationId: string;

  @Column({
    type: 'enum',
    enum: EvaluationCycleStatus,
    default: EvaluationCycleStatus.DRAFT,
  })
  status: EvaluationCycleStatus;

  @Column({ type: 'timestamp with time zone' })
  startDate: Date;

  @Column({ type: 'timestamp with time zone' })
  endDate: Date;

  @Column({ default: true })
  allowSelfEvaluation: boolean;

  @Column({ default: true })
  allowPeerEvaluation: boolean;

  @Column({ default: true })
  allowManagerEvaluation: boolean;

  @Column({ default: true })
  allowSubordinateEvaluation: boolean;

  @Column({ default: false })
  allowCustomerEvaluation: boolean;

  @Column({ default: 48 })
  inviteValidityHours: number;

  @Column({ default: true })
  isAnonymous: boolean;

  @Column({ type: 'jsonb', nullable: true })
  evaluationCriteria: Record<string, any>;

  @OneToMany(() => EvaluationInvitation, (invitation) => invitation.cycle)
  invitations: EvaluationInvitation[];

  @OneToMany(() => EvaluationResponse, (response) => response.cycle)
  responses: EvaluationResponse[];

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
