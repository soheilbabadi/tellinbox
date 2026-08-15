import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { EvaluationInvitation } from './evaluation-invitation.entity';
import { EvaluationCycle } from './evaluation-cycle.entity';

@Entity('evaluation_response')
export class EvaluationResponse {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => EvaluationInvitation, (invitation) => invitation.responses, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'invitation_id' })
  invitation: EvaluationInvitation;

  @Column({ type: 'uuid' })
  invitationId: string;

  @ManyToOne(() => EvaluationCycle, (cycle) => cycle.responses)
  @JoinColumn({ name: 'cycle_id' })
  cycle: EvaluationCycle;

  @Column({ type: 'uuid' })
  cycleId: string;

  @Column({ type: 'jsonb' })
  answers: Record<string, any>;

  @Column({ type: 'int', nullable: true })
  totalScore: number | null;

  @Column({ type: 'text', nullable: true })
  comments: string | null;

  @Column({ default: false })
  isSubmitted: boolean;

  @CreateDateColumn()
  submittedAt: Date | null;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
