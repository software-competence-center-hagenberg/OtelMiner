'use client'
import React, {useEffect, useState} from 'react';
import TraceDetailsView from './TraceDetailsView';
import RestService from "@/app/lib/RestService";
import {CircularProgress, Grid2, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

interface DataOverviewProps {
    nrNodes: number[];
    nrTraces: number;
    sourceFile: string;
}

interface Column extends ColumnBase {
    id: 'nrNodes' | 'nrTraces' | 'sourceFile';
}

const columns: readonly Column[] = [
    {id: 'nrNodes', label: 'Number of Nodes', minWidth: 100},
    {id: 'nrTraces', label: 'Number of Traces', minWidth: 100},
    {id: 'sourceFile', label: 'Source File', minWidth: 100}
];

const DataOverview: React.FC = () => {
    const [data, setData] = useState<DataOverviewProps[]>([]);
    const [sourceFile, setSourceFile] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<unknown | null>(null);

    useEffect(() => {
        RestService.get<DataOverviewProps[]>('/overview')
            .then((response) => {
                setData(response.data);
                setLoading(false);
            })
            .catch((error) => {
                setError(error);
                setLoading(false);
            });
    }, []);

    const handleRowClick = (sourceFile: string) => {
        if (sourceFile) {
            setSourceFile(sourceFile);
        }
    };

    const renderTable = () => {
        return (
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
                        {data.map((row) => (
                            <TableRow
                                onClick={() => handleRowClick(row.sourceFile)}
                                tabIndex={-1}
                                key={row.sourceFile}
                            >
                                {columns.map((column) => (
                                    <TableCell key={column.id} align={column.align}>
                                        {JSON.stringify(row[column.id])}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        );
    };

    const content = () => {
        if (loading) {
            return (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <CircularProgress/>
                </Box>
            )
        }
        if (error) {
            return (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <Typography variant="h6">Error loading data</Typography>
                </Box>
            );
        }
        return renderTable();
    }

    return (
        <Grid2 container spacing={2} columns={12}>
            <Grid2 size={3}>
                <Typography variant="h3">Overview</Typography>
                {content()}
            </Grid2>
            <Grid2 size={"grow"}>
                <Box height="100%">
                    {sourceFile && <TraceDetailsView sourceFile={sourceFile}/>}
                </Box>
            </Grid2>
        </Grid2>
    );
};

export default DataOverview;