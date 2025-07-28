'use client'
import React, {useMemo, useState} from 'react';
import TraceDetailsView from './TraceDetailsView';
import RestService from "@/app/lib/RestService";
import {
    Button,
    CircularProgress,
    Grid2,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import ProbDeclareView from "@/app/ui/ProbDeclareView";
import {ColumnBase} from "@/app/lib/Util";

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
    const [nrTraces, setNrTraces] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<unknown | null>(null);
    const [generatingProbDeclare, setGeneratingProbDeclare] = useState<boolean>(false);

    const fetchDataOverview = () => {
        RestService.get<DataOverviewProps[]>('/overview')
            .then((response) => {
                setData(() => response.data);
                setLoading(false);
            })
            .catch((error) => {
                setError(() => error);
                setLoading(false);
            });
    }

    useMemo(fetchDataOverview, []);

    const handleRowClick = (row: DataOverviewProps) => {
        setSourceFile(() => row.sourceFile);
        setNrTraces(() => row.nrTraces);
        setGeneratingProbDeclare(() => false);
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
                                onClick={() => handleRowClick(row)}
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

    const renderDataOverview = () => {
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
        return <Box>
            {renderTable()}
            <Button
                variant={"contained"}
                onClick={() => setGeneratingProbDeclare(true)}
                disabled={!sourceFile || generatingProbDeclare}
            >
                generate PB Model
            </Button>
        </Box>
    }

    return (
                <Box height="100%">
                    <Typography variant="h3">Overview</Typography>
                    {renderDataOverview()}
                    {sourceFile && !generatingProbDeclare && <TraceDetailsView sourceFile={sourceFile}/>}
                    {sourceFile && generatingProbDeclare && <ProbDeclareView sourceFile={sourceFile} expectedTraces={nrTraces!} closeCallBack={() => setGeneratingProbDeclare(false)}/>}
                </Box>
    );
};

export default DataOverview;