'use client';
import React, {Component} from 'react';
import TraceDetailsView from './TraceDetailsView';
import RestService from "@/app/lib/RestService";
import {Grid2, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

interface DataOverviewProps {
    nrNodes: number[];
    nrTraces: number;
    sourceFile: string;
}

interface DataOverviewState {
    data: DataOverviewProps[];
    sourceFile: string | null;
    loading: boolean;
}

interface Column extends ColumnBase {
    id: 'nrNodes' | 'nrTraces' | 'sourceFile';
}

const columns: readonly Column[] = [
    {id: 'nrNodes', label: 'Number of Nodes', minWidth: 100},
    {id: 'nrTraces', label: 'Number of Traces', minWidth: 100},
    {id: 'sourceFile', label: 'Source File', minWidth: 100}
];

const restService = new RestService();

class DataOverview extends Component<{}, DataOverviewState> {
    constructor(props: {}) {
        super(props);
        this.state = {
            data: [],
            sourceFile: null,
            loading: true,
        };
    }

    componentDidMount() {
        restService.get<DataOverviewProps[]>('/overview')
            .then((response: { data: DataOverviewProps[]; }) => this.setState({
                sourceFile: null,
                data: response.data, loading: false
            }))
            .catch((error: any) => {
                console.error('Error fetching data:', error);
                this.setState({loading: false});
            });
    }

    private readonly handleRowClick = (sourceFile: string) => {
        this.setState({sourceFile});
    }

    render() {
        const {data, sourceFile, loading} = this.state;

        return (
            // <Box height="50%" display="flex" flexDirection="row">
            <Grid2 container spacing={2} columns={12}>
                {/*<Box height="100%" display="flex" flexDirection="column">*/}
                <Grid2 size={3}>
                    <Typography variant="h3">Overview</Typography>
                    <TableContainer sx={{flex: "1 1 auto", overflowY: "auto"}}>
                        <Table stickyHeader aria-label="data-overview">
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
                                {data
                                    .map((row) => {
                                        return (
                                            <TableRow
                                                onClick={() => this.handleRowClick(row.sourceFile)}
                                                tabIndex={-1}
                                                key={row.sourceFile}>
                                                {columns.map((column) => {
                                                    const value = row[column.id];
                                                    return (
                                                        <TableCell key={column.id} align={column.align}>
                                                            {JSON.stringify(value)}
                                                        </TableCell>
                                                    );
                                                })}
                                            </TableRow>
                                        );
                                    })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Grid2>
                {/*</Box>*/}
                {/*<Box height="100%" display="flex" flexDirection="column">*/}
                {/*<Divider>*/}
                {/*    <Chip label="Trace Details" size="small" />*/}
                {/*</Divider>*/}
                <Grid2 size={"grow"}>
                    <Box height="100%">
                        {sourceFile && (<TraceDetailsView sourceFile={sourceFile} restService={restService}/>)}
                    </Box>
                </Grid2>
                {/*</Box>*/}

            </Grid2>
            // </Box>
        );
    }
}

export default DataOverview;
