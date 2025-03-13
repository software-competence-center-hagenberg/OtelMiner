export interface ColumnBase {
    label: string;
    minWidth?: number;
    align?: 'right';
    format?: (value: number) => string;
}

export interface TraceDetails {
    traceId?: string;
    nrNodes?: number;
    spans?: Buffer[]; // large strings -> buffer
}

export interface SourceDetails {
    sourceFile: string;
    traces: TraceDetails[];
    page: number;
    size: number;
    totalPages: number;
    sort: string;
}

export function defaultSourceDetails(sourceFile: string) {
    return {
        sourceFile: sourceFile,
        traces: [],
        page: 1,
        size: 10,
        totalPages: 1,
        sort: "sourceFile"
    }
}