'use client';
import React, {Component} from 'react';
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
import JsonViewer from "@/app/ui/JsonViewer";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

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
    restService: RestService;
}

interface TraceDetailsTableState {
    sourceDetails: SourceDetails;
    selectedRow: TraceDetails | null;
    selectedRowModel: string | null; // FIXME evaluate, probably better to integrate in TraceDetails
    loading: boolean;
}

class TraceDetailsView extends Component<TraceDetailsTableProps, TraceDetailsTableState> {
    constructor(props: TraceDetailsTableProps) {
        super(props);
        this.state = this.initState();
    }

    private initState = () => {
        return {
            sourceDetails: this.buildSourceDetails(this.props.sourceFile),
            selectedRow: null,
            selectedRowModel: null,
            loading: true,
        }
    }

    componentDidMount = () => {
        this.updateData();
    }

    componentDidUpdate = (prevProps: TraceDetailsTableProps, prevState: TraceDetailsTableState) => {
        if (prevProps.sourceFile !== this.props.sourceFile) {
            this.setState(() => this.initState, this.updateData);
        } else if (prevState.sourceDetails.page !== this.state.sourceDetails.page
            || prevState.sourceDetails.size !== this.state.sourceDetails.size) {
            this.updateData();
        }
    }

    private buildSourceDetails = (sourceFile: string) => {
        return {
            sourceFile: sourceFile,
            traces: [],
            page: 1,
            size: 10,
            totalPages: 1,
            sort: "sourceFile"
        }
    }

    private updateData = () => {
        const {restService} = this.props;
        const {sourceDetails} = this.state;
        this.setState({loading: true});
        restService.post('/details', sourceDetails)
            .then((response) => this.updateState(response.data))
            .catch((error) => this.handleError(error, 'Error fetching trace details:'));
    }

    private updateState = (sd: SourceDetails) => {
        this.setState((prevState) => ({
            sourceDetails: {
                ...prevState.sourceDetails,
                traces: sd.traces,
                totalPages: sd.totalPages,
                size: sd.size,
                page: sd.page
            },
            selectedRow: null,
            selectedRowModel: null,
            loading: false,
        }));
    }

    private handleError = (errorMessage: string, error?: any) => {
        console.error(errorMessage, error);
        this.setState({loading: false});
    }

    private handlePageChange = (_event: unknown, newPage: number) => {
        this.setState(
            (prevState) => ({
                sourceDetails: {...prevState.sourceDetails, page: newPage},
                selectedRow: null,
                selectedRowModel: null
            }),
            this.updateData
        );
    }

    private handlePageSizeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        this.setState(
            (prevState) => ({
                sourceDetails: {...prevState.sourceDetails, size: +event.target.value, page: 0},
                selectedRow: null,
                selectedRowModel: null
            }),
            this.updateData
        );
    }

    private handleRowClick = (selectedRow: TraceDetails) => {
        this.setState((prevState) => ({sourceDetails: {...prevState.sourceDetails}, selectedRow: selectedRow}));
    }

    private onClickGenerateModel = () => {
        const {restService} = this.props;
        const {selectedRow} = this.state;
        if (!selectedRow) {
            return;
        }
        this.setState((prevState) => ({
            sourceDetails: {...prevState.sourceDetails},
            selectedRow: {...prevState.selectedRow},
            selectedRowModel: null,
            loading: true
        }));
        restService.post("/generate-model", selectedRow)
            .then((response) => this.pollModel(response.data, ""))
            .catch((error) => this.handleError("Error generating model: ", error));
    }

    private pollModel = (traceId: string, model: string) => {
        const {restService} = this.props;
        if (model === "") { // FIXME quick and dirty solution
            restService.get("/model/" + traceId)
                .then((response)  => this.pollModel(traceId, response.data))
                .catch((error) => this.handleError("Error generating model: ", error));
        } else {
            this.updateSelectedRowModel(model);
        }
    }

    private updateSelectedRowModel = (model: string | null) => {
        if (model) {
            this.handleError("error: empty model!");
        }
        this.setState((prevState) => ({
            sourceDetails: {...prevState.sourceDetails},
            selectedRow: {...prevState.selectedRow},
            selectedRowModel: model,
            loading: false
        }));
    }

    render = () => {
        const {sourceDetails, loading, selectedRow, selectedRowModel} = this.state;
        return (
            <Box>
                <Typography variant="h4">Trace Details</Typography>
                {loading ? (
                    <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                        <CircularProgress/>
                    </Box>
                ) : (
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
                                    {sourceDetails.traces
                                        .map((row) => {
                                            return (
                                                <TableRow
                                                    onClick={() => this.handleRowClick(row)}
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
                                        })}
                                </TableBody>
                            </Table>
                        </TableContainer>
                        <TablePagination
                            rowsPerPageOptions={[10, 25, 100]}
                            component="div"
                            count={sourceDetails.totalPages}
                            rowsPerPage={sourceDetails.size}
                            page={sourceDetails.page}
                            onPageChange={this.handlePageChange}
                            onRowsPerPageChange={this.handlePageSizeChange}
                        />
                        {selectedRow && (
                            <Grid2 container spacing={2}>
                                <Grid2 size={6} columns={6}>
                                    <Box>
                                        <Button
                                            variant={"contained"}
                                            onClick={this.onClickGenerateModel}
                                        >
                                            generate Model
                                        </Button>
                                        {selectedRow.spans && (<JsonViewer data={selectedRow.spans.map(s => JSON.parse(s.toString()))}/>)}
                                    </Box>
                                </Grid2>
                                {selectedRowModel && (
                                    <Grid2 size={6} columns={6}>
                                        <JsonViewer data={selectedRowModel}/>
                                    </Grid2>
                                )}
                            </Grid2>
                        )}
                    </Grid2>
                )}
            </Box>
        );
    }

}

export default TraceDetailsView;
