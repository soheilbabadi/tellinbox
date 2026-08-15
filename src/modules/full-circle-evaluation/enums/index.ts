export enum EvaluationCycleStatus {
  DRAFT = 'draft',
  ACTIVE = 'active',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
}

export enum EvaluatorRelationship {
  SELF = 'self',
  MANAGER = 'manager',
  PEER = 'peer',
  SUBORDINATE = 'subordinate',
  CUSTOMER_INTERNAL = 'customer_internal',
  CUSTOMER_EXTERNAL = 'customer_external',
}

export enum InvitationStatus {
  PENDING = 'pending',
  ACCEPTED = 'accepted',
  COMPLETED = 'completed',
  EXPIRED = 'expired',
  CANCELLED = 'cancelled',
}
