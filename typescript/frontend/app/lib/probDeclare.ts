export interface DeclareConstraint {
    probability: number;
    declareTemplate: string;
    nr: number
}

export interface ProbDeclare {
    id: string;
    generating: boolean;
    paused: boolean,
    tracesProcessed: number,
    constraints: DeclareConstraint[];
    traces: string[];
}

export interface ProbDeclareInfo {
    id: string,
    insertDate: Date,
    updateDate: Date,
    generating: boolean,
}