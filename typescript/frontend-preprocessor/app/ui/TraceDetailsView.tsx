import React, {useEffect, useState} from 'react';
import {
    Button,
    CircularProgress,
    Grid2,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow
} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import JsonView from "@/app/ui/json/JsonView";
import {SimpleTreeView} from '@mui/x-tree-view/SimpleTreeView';
import {TreeItem} from '@mui/x-tree-view/TreeItem';

interface Column extends ColumnBase {
    id: "traceId" | "nrNodes";
}

const columns: readonly Column[] = [
    {id: 'traceId', label: 'Trace ID', minWidth: 100},
    {id: 'nrNodes', label: 'Number of Nodes', minWidth: 100, format: (value: number) => value.toString()}
];

interface TraceDetails {
    traceId?: string;
    nrNodes?: number;
    spans?: Buffer[]; // large strings -> buffer
}

interface SourceDetails {
    sourceFile: string;
    traces: TraceDetails[];
    page: number;
    size: number;
    totalPages: number;
    sort: string;
}

interface TraceDetailsTableProps {
    sourceFile: string;
}

interface KeyValue {
    key: string;
    value: string;
}

interface SpanEvent {
    time_unix_nano: string;
    name: string;
    attributes: KeyValue[];
    dropped_attributes_count: string;
}

interface SpanLink {
    trace_id: string;
    span_id: string;
    trace_state: string;
    attributes: KeyValue[];
    dropped_attributes_count: string;
}

interface Status {
    message: string;
    code: string;
}

interface Span {
    trace_id: string;
    span_id: string;
    trace_state: string;
    parent_span_id: string;
    name: string;
    kind: string;
    start_time_unix_nano: string;
    end_time_unix_nano: string;
    attributes: KeyValue[];
    dropped_attributes_count: string;
    events: SpanEvent[]
    dropped_events_count: string;
    links: SpanLink[];
    dropped_links_count: string;
    status: Status | undefined;
}

interface SpanTreeNode {
    span: Span;
    children: SpanTreeNode[];
}

interface SpanTreeModel {
    traceId: string;
    spanTrees: SpanTreeNode[];
}

function defaultSourceDetails(sourceFile: string) {
    return {
        sourceFile: sourceFile,
        traces: [],
        page: 1,
        size: 10,
        totalPages: 1,
        sort: "sourceFile"
    }
}

const TraceDetailsView = ({sourceFile}: TraceDetailsTableProps) => {
    const [sourceDetails, setSourceDetails] = useState<SourceDetails>(defaultSourceDetails(sourceFile));
    const [selectedRow, setSelectedRow] = useState<TraceDetails>(); // FIXME check if better with useRef
    const [selectedRowSpanTrees, setSelectedRowSpanTrees] = useState<SpanTreeModel | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setSelectedRow(undefined); // Clear previous row selection
        setSelectedRowSpanTrees(null); // clear
        setLoading(true); // Reset loading state
        fetchSourceDetails(sourceDetails);
    }, [sourceDetails.sourceFile, sourceDetails.page, sourceDetails.size]);

    useEffect(() => {
        setSourceDetails((prevDetails) => ({
            ...prevDetails,
            sourceFile: sourceFile,
        }));
    }, [sourceFile]);

    const handlePageChange = (_event: unknown, newPage: number) => {
        setSourceDetails((prevDetails) => ({
            ...prevDetails,
            page: newPage,
        }));
    };

    const fetchSourceDetails = (sourceDetails: SourceDetails) => {
        setLoading(true);
        RestService.post<SourceDetails, SourceDetails>('/details', sourceDetails)
            .then((response) => setSourceDetails(response.data))
            .catch((error) => console.error('Error fetching source details:', error))
            .finally(() => setLoading(false));
    };

    const handlePageSizeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const newSize = +event.target.value;
        setSourceDetails((prevDetails) => ({
            ...prevDetails,
            page: 0,
            size: newSize,
        }));
    };

    const handleRowClick = (selectedRow: TraceDetails) => {
        setSelectedRowSpanTrees(null);
        setSelectedRow(selectedRow);
    };

    const onClickGenerateModel = async () => {
        try {
            setLoading(true);
            const response = await RestService.post<TraceDetails, string>("/generate-span-trees", selectedRow!);
            const model = await pollModel(response.data);
            setSelectedRowSpanTrees((_prev) => JSON.parse(JSON.stringify(model)));
        } catch (error) {
            console.error('Error generating model:', error);
        } finally {
            setLoading(false);
        }
    };

    const pollModel = async (traceId: string): Promise<string> => {
        return new Promise((resolve, reject) => {
            const intervalId = setInterval(async () => {
                try {
                    const response = await RestService.get<string>("/model/" + traceId);
                    if (response.data !== '') {
                        clearInterval(intervalId); // Stop polling
                        console.log(response.data);
                        resolve(response.data);
                    }
                } catch (error) {
                    clearInterval(intervalId);
                    reject(error);
                }
            }, 2000); // Poll every 2 seconds
        });
    };


    const renderTableRow = (row: TraceDetails) => {
        return (
            <TableRow
                onClick={() => handleRowClick(row)}
                tabIndex={-1}
                key={row.traceId}>
                {columns.map((column) => {
                    const value = row[column.id];
                    return (
                        <TableCell key={column.id} align={column.align}>
                            {column.format && typeof value === 'number'
                                ? column.format(value)
                                : value}
                        </TableCell>
                    );
                })}
            </TableRow>
        );
    }
    const renderJsonView = (selectedRow: TraceDetails) => {
        return <Grid2 size={6} columns={6}>
            <Box>
                <Button variant={"contained"} onClick={onClickGenerateModel}>
                    generate Model
                </Button>
                {selectedRow.spans && (
                    <JsonView data={selectedRow.spans.map(s => JSON.parse(s.toString()))}/>
                )}
            </Box>
        </Grid2>;
    }

    const renderSpanTree = (spanTree: SpanTreeNode) => {
        if (spanTree.children.length === 0) {
            return <TreeItem itemId={spanTree.span.span_id} label={JSON.stringify(spanTree.span)}/>
        }
        return <TreeItem itemId={spanTree.span.span_id} label={JSON.stringify(spanTree.span)}>
            {spanTree.children.map(child => renderSpanTree(child))}
        </TreeItem>
    }

    const renderTable = () => {
        return (
            <Grid2 container>
                <TableContainer sx={{maxHeight: 440}}>
                    <Table stickyHeader aria-label="trace-details">
                        <TableHead>
                            <TableRow>
                                {columns.map((column) => (
                                    <TableCell
                                        key={column.id}
                                        align={column.align}
                                        style={{minWidth: column.minWidth}}
                                    >
                                        {column.label}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {sourceDetails.traces.map((row) => renderTableRow(row))}
                        </TableBody>
                    </Table>
                </TableContainer>
                <TablePagination
                    rowsPerPageOptions={[10, 25, 100]}
                    component="div"
                    count={sourceDetails.totalPages}
                    rowsPerPage={sourceDetails.size}
                    page={sourceDetails.page}
                    onPageChange={handlePageChange}
                    onRowsPerPageChange={handlePageSizeChange}
                />
                {selectedRow && (
                    <Grid2 container spacing={2}>
                        {renderJsonView(selectedRow)}
                        {selectedRowSpanTrees && (
                            <Grid2 size={6} columns={6}>
                                {selectedRowSpanTrees.spanTrees.map(spanTree => {
                                    return <SimpleTreeView
                                        key={selectedRowSpanTrees.traceId + selectedRowSpanTrees.spanTrees.indexOf(spanTree)}>
                                        {renderSpanTree(spanTree)}
                                    </SimpleTreeView>;
                                })
                                }
                                {/*<JsonView data={selectedRowSpanTrees}/>*/}
                            </Grid2>
                        )}
                    </Grid2>
                )}
            </Grid2>
        );
    }

    return (
        <Box>
            <Typography variant="h4">Trace Details</Typography>
            {loading ? (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <CircularProgress/>
                </Box>
            ) : (
                renderTable()
            )}
        </Box>
    );
};

export default TraceDetailsView;