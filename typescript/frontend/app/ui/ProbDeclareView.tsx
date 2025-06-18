import React, {useMemo, useState} from "react";
import {
    Button,
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import JsonView from "@/app/ui/json/JsonView";
import {AxiosResponse} from "axios";
import {defaultSourceDetails, SourceDetails} from "@/app/lib/Util";

interface ProbDeclareViewProps {
    sourceFile: string;
    expectedTraces: number;
    abortCallback: () => void;
}

interface DeclareConstraint {
    probability: number;
    declareTemplate: string;
}

interface ProbDeclare {
    id: string;
    generating: boolean;
    constraints: DeclareConstraint[];
    traces: string[];
}

const ProbDeclareView = ({sourceFile, expectedTraces, abortCallback}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState<boolean>(false);
    const [initialized, setInitialized] = useState<boolean>(false);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);
    const [aborting, setAborting] = useState<boolean>(false);
    const [paused, setPaused] = useState<boolean>(false);
    const timerRef = React.useRef<NodeJS.Timeout | null>(null);

    const handleProbDeclareResponse = (response: AxiosResponse<any, ProbDeclare>) => {
        const probDeclare: ProbDeclare = response.data;
        setProbDeclare((prev) => (prev ? {
            ...prev,
            constraints: probDeclare.constraints,
            generating: probDeclare.generating,
        } : probDeclare));

        if (aborting) {
            abort();
        }
    }

    const initModelGeneration = () => {
        setLoading(true);
        const sourceDetails: SourceDetails = defaultSourceDetails(sourceFile);
        RestService.post<SourceDetails, ProbDeclare>("/prob-declare/generate?expected-traces=" + expectedTraces, sourceDetails)
            .then((response) => {
                setInitialized(true);
                handleProbDeclareResponse(response);
            })
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(() => false));
    };

    const clearTimer = () => {
        if (timerRef.current) {
            clearTimeout(timerRef.current);
            timerRef.current = null;
        }
    }

    const fetchModel = async () => {
        clearTimer();

        try {
            const response = await RestService.get<ProbDeclare>("/prob-declare/" + probDeclare!.id);
            handleProbDeclareResponse(response);
        } catch (error) {
            console.error('Error fetching prob declare model', error);
        }
    }

    const updateModel = () => {
        if (paused || aborting || !initialized) {
            return;
        }
        if (probDeclare?.generating) {
            timerRef.current = setTimeout(fetchModel, 2000);
        }
    }

    const reset = () => {
        clearTimer();
        if (paused || probDeclare?.generating) {
            setPaused(false);
            abort();
        } else {
            setInitialized(false);
            setProbDeclare(null);
        }
    }

    useMemo(reset, [sourceFile])
    useMemo(updateModel, [probDeclare?.constraints, probDeclare?.generating]);

    const pause = () => {
        console.debug("model initialized --> sending pause request.")

        clearTimer();

        RestService.get<void>("/prob-declare/pause/" + probDeclare!.id)
            .then((_) => setPaused(true))
            .catch((error) => console.error('Error pausing generation of prob declare model', error));
    }

    const resume = () => {
        console.debug("model initialized --> sending resume request.")
        RestService.get<void>("/prob-declare/resume/" + probDeclare!.id)
            .then((_) => {
                setPaused(false);
            })
            .catch((error) => console.error('Error pausing generation of prob declare model', error))
            .finally(fetchModel);
    }

    const abort = () => {
        setAborting(true);
        clearTimer();
        if (initialized) {
            console.debug("model initialized --> sending abort request to backend.")
            RestService.get<void>("/prob-declare/abort/" + probDeclare?.id)
                .then(close)
                .catch((error) => console.error('Error aborting generation of prob declare model', error));
        } else {
            console.debug("model NOT initialized --> aborting in callback.")
            // abortCallback();
        }
    }
    const close = () => {
        reset();
        abortCallback();
    }

    return (
        <>
            <Box>
            <Typography variant="h4">Prob Declare Modell for {sourceFile}</Typography>
            {loading && !probDeclare ? (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <CircularProgress/>
                </Box>
            ) : (
                probDeclare && (
                    <TableContainer component={Paper}>
                        <Table sx={{minWidth: 650}} size="small" aria-label="a dense table">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Prob Declare
                                        Id: {probDeclare.id} (generating: {probDeclare.generating ? "true" : "false"})</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell align="right">Probability</TableCell>
                                    <TableCell>Declare Constraint</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {probDeclare.constraints
                                    .toSorted((a, b) => b.probability - a.probability)
                                    .map((row) => (
                                        <TableRow
                                            key={row.declareTemplate}
                                            sx={{'&:last-child td, &:last-child th': {border: 0}}}
                                        >
                                            <TableCell align="right">{row.probability}</TableCell>
                                            <TableCell component="th" scope="row">
                                                {row.declareTemplate}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )
            )}
            <Button
                variant={'contained'}
                onClick={initModelGeneration}
                disabled={aborting || initialized}
            >
                start
            </Button>
            <Button
                variant={'contained'}
                onClick={pause}
                disabled={paused || aborting || !probDeclare?.generating}
            >
                pause
            </Button>
            <Button
                variant={'contained'}
                onClick={resume}
                disabled={!paused || aborting || !probDeclare?.generating}
            >
                resume
            </Button>
                <Button
                    variant={'contained'}
                    onClick={abort}
                    disabled={aborting || !probDeclare?.generating}
                >
                    abort
                </Button>
                <Button
                    variant={'contained'}
                    onClick={close}
                    disabled={aborting || probDeclare?.generating && !paused}
                >
                    close
                </Button>
            </Box>
            <JsonView data={probDeclare}/>
        </>
    );
};

export default ProbDeclareView;