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
import DeclareView from "@/app/ui/DeclareView";
import JsonView from "@/app/ui/json/JsonView";
import {defaultSourceDetails, ColumnBase, SourceDetails, TraceDetails} from "@/app/lib/Util";

interface Column extends ColumnBase {
    id: "traceId" | "nrNodes";
}

const columns: readonly Column[] = [
    {id: 'traceId', label: 'Trace ID', minWidth: 100},
    {id: 'nrNodes', label: 'Number of Nodes', minWidth: 100, format: (value: number) => value.toString()}
];

interface TraceDetailsTableProps {
    sourceFile: string;
}

const TraceDetailsView = ({sourceFile}: TraceDetailsTableProps) => {
    const [sourceDetails, setSourceDetails] = useState<SourceDetails>(defaultSourceDetails(sourceFile));
    const [selectedRow, setSelectedRow] = useState<TraceDetails>(); // FIXME check if better with useRef
    const [selectedRowModel, setSelectedRowModel] = useState<string[] | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchSourceDetails(sourceDetails);
    }, [sourceFile]);

    const handlePageChange = (_event: unknown, newPage: number) => {
        const sd = sourceDetails;
        sd.page = newPage;
        fetchSourceDetails(sd);
    };

    const fetchSourceDetails = (sourceDetails: SourceDetails) => {
        setLoading(true);
        RestService.post<SourceDetails, SourceDetails>('/details', sourceDetails)
            .then((response) => setSourceDetails(() => response.data))
            .catch((error) => console.error('Error fetching source details:', error))
            .finally(() => setLoading(false));
    };

    const handlePageSizeChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const sd = sourceDetails;
        sd.size = +event.target.value;
        fetchSourceDetails(sd);
    };

    const handleRowClick = (selectedRow: TraceDetails) => {
        setSelectedRow(() => selectedRow);
    };

    const onClickGenerateModel = async () => {
        try {
            setLoading(true);
            const response = await RestService.post<TraceDetails, string>("/declare/generate", selectedRow!);
            const model = await pollModel(response.data);
            setSelectedRowModel(() => JSON.parse(JSON.stringify(model)));
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
                    const response = await RestService.get<string>("/declare/" + traceId);
                    if (response.data !== '') {
                        clearInterval(intervalId); // Stop polling
                        console.log(response.data);
                        resolve(response.data);
                    }
                } catch (error) {
                    console.error('Error polling model:', error);
                    clearInterval(intervalId)
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
                        {selectedRowModel && (
                            <Grid2 size={6} columns={6}>
                                <DeclareView rawData={selectedRowModel}/>
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