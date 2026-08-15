import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { EvaluationCycle } from './entities/evaluation-cycle.entity';
import { EvaluationInvitation } from './entities/evaluation-invitation.entity';
import { EvaluationResponse } from './entities/evaluation-response.entity';
import { Evaluation360Service } from './services/full-circle-evaluation.service';
import { Evaluation360Controller } from './controllers/full-circle-evaluation.controller';
import { User } from '../user/entities/user.entity';
import { Organization } from '../organization/entities/organization.entity';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      EvaluationCycle,
      EvaluationInvitation,
      EvaluationResponse,
      User,
      Organization,
    ]),
  ],
  controllers: [Evaluation360Controller],
  providers: [Evaluation360Service],
  exports: [Evaluation360Service],
})
export class Evaluation360Module {}
