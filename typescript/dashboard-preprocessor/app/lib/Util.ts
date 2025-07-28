interface ColumnBase {
    label: string;
    minWidth?: number;
    align?: 'right';
    format?: (value: number) => string;
}
