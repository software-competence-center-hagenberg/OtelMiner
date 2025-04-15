import React, {useMemo, useRef, useState} from 'react';
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
import {ColumnBase, defaultSourceDetails, SourceDetails, TraceDetails} from "@/app/lib/Util";

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
    const [loadingPage, setLoadingPage] = useState<boolean>(false);
    const [loadingModel, setLoadingModel] = useState<boolean>(false);
    const [selectedRow, setSelectedRow] = useState<TraceDetails | undefined>(undefined);
    const [selectedRowModel, setSelectedRowModel] = useState<string[] | undefined>(undefined);

    const fetchSourceDetails = () => {
        setLoadingPage(true)
        RestService.post<SourceDetails, SourceDetails>('/details', sourceDetails)
            .then((response) => setSourceDetails(() => response.data))
            .catch((error) => console.error('Error fetching source details:', error))
            .finally(() => setLoadingPage(false));
    };

    useMemo(() => setSourceDetails(
        _prev => (defaultSourceDetails(sourceFile))
    ), [sourceFile])
    useMemo(fetchSourceDetails, [sourceDetails.sourceFile, sourceDetails.page, sourceDetails.size]);

    const handlePageChange = (_event: unknown, newPage: number) => {
        setSourceDetails(prev => ({
            ...prev,
            page: newPage,
        }));
        setSelectedRow(undefined);
        setSelectedRowModel(undefined);
    };

    const handlePageSizeChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        setSourceDetails(prev => ({
            ...prev,
            page: 0,
            size: +event.target.value
        }));
    };

    const handleRowClick = (newRow: TraceDetails) => {
        setSelectedRow(newRow);
        setSelectedRowModel(undefined);
    };

    const onClickGenerateModel = async () => {
        try {
            setLoadingModel(true)
            const response = await RestService.post<TraceDetails, string>("/declare/generate", selectedRow!);
            const model = await pollModel(response.data);
            const rowModel: string[] = JSON.parse(JSON.stringify(model));
            setSelectedRowModel(rowModel);
        } catch (error) {
            console.error('Error generating model:', error);
        } finally {
            setLoadingModel(false);
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
                    reject(new Error("Error polling model"));
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
    const renderJsonView = () => {
        return <Grid2 size={6} columns={6}>
            <Box>
                <Button variant={"contained"} onClick={onClickGenerateModel}>
                    generate Model
                </Button>
                {selectedRow!.spans && (
                    <JsonView data={selectedRow!.spans.map(s => JSON.parse(s.toString()))}/>
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
                            {sourceDetails.traces.map((row:TraceDetails) => renderTableRow(row))}
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
                        {renderJsonView()}
                        {selectedRowModel && (
                            <Grid2 size={6} columns={6}>
                                {loadingModel
                                    ? <CircularProgress size={'3rem'}/> // FIXME not rendered yet
                                    : <DeclareView rawData={selectedRowModel}/>}
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
            {loadingPage ? (
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