import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  OneToMany,
  JoinColumn,
  Unique,
} from 'typeorm';
import { User } from '../../user/entities/user.entity';
import { EvaluationCycle } from './evaluation-cycle.entity';
import { EvaluatorRelationship, InvitationStatus } from '../enums';

@Entity('evaluation_invitation')
@Unique(['cycle', 'evaluator', 'evaluatee', 'relationship'])
export class EvaluationInvitation {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => EvaluationCycle, (cycle) => cycle.invitations, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'cycle_id' })
  cycle: EvaluationCycle;

  @Column({ type: 'uuid' })
  cycleId: string;

  @ManyToOne(() => User, { nullable: true })
  @JoinColumn({ name: 'evaluator_id' })
  evaluator: User | null;

  @Column({ type: 'uuid', nullable: true })
  evaluatorId: string | null;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'evaluatee_id' })
  evaluatee: User;

  @Column({ type: 'uuid' })
  evaluateeId: string;

  @Column({
    type: 'enum',
    enum: EvaluatorRelationship,
  })
  relationship: EvaluatorRelationship;

  @Column({
    type: 'enum',
    enum: InvitationStatus,
    default: InvitationStatus.PENDING,
  })
  status: InvitationStatus;

  @Column({ type: 'varchar', unique: true })
  inviteToken: string;

  @Column({ type: 'timestamp with time zone' })
  expiresAt: Date;

  @Column({ type: 'timestamp with time zone', nullable: true })
  acceptedAt: Date;

  @Column({ type: 'timestamp with time zone', nullable: true })
  completedAt: Date;

  @Column({ type: 'text', nullable: true })
  customMessage: string;

  @OneToMany(() => EvaluationResponse, (response) => response.invitation)
  responses: EvaluationResponse[];

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
