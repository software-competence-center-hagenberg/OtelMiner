import React, {useMemo, useRef, useState} from "react";
import {
    Button,
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField
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

interface ProbDeclareInfo {
    id: string,
    insertDate: Date,
    updateDate: Date,
    generating: boolean
}

const ProbDeclareView = ({sourceFile, expectedTraces, abortCallback}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState<boolean>(false);
    const [initialized, setInitialized] = useState<boolean>(false);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);
    const [aborting, setAborting] = useState<boolean>(false);
    const [paused, setPaused] = useState<boolean>(false);
    const [sourceDetails, setSourceDetails] = useState<SourceDetails>(defaultSourceDetails(sourceFile));
    const [actualExpectedTraces, setActualExpectedTraces] = useState<number>(expectedTraces);
    const [nrSegments, setNrSegments] = useState<number>(0);
    const [segmentSize, setSegmentSize] = useState<number>(-1);
    const [existingModels, setExistingModels] = useState<ProbDeclareInfo[]>([]);
    const [loadingExistingModels, setLoadingExistingModels] = useState<boolean>(false);

    const timerRef = useRef<NodeJS.Timeout | null>(null);

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
        RestService.post<SourceDetails, ProbDeclare>(
            "/prob-declare/generate?expected-traces=" + actualExpectedTraces
            + "&nr-segments=" + nrSegments
            + "&segment-size=" + segmentSize
            , sourceDetails)
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

    function onStartPageChanged(event: React.ChangeEvent<HTMLInputElement>) {
        console.log("probDeclareView: changing start page to " + event.target.valueAsNumber)
        setSourceDetails(prev => ({
            ...prev,
            page: event.target.valueAsNumber
        }));
    }

    function onPageSizeChanged(event: React.ChangeEvent<HTMLInputElement>) {
        console.log("probDeclareView: changing page size to " + event.target.valueAsNumber)
        setSourceDetails(prev => ({
            ...prev,
            size: event.target.valueAsNumber
        }));
    }

    function onActualExpectedTracesChanged(event: React.ChangeEvent<HTMLInputElement>) {
        console.log("probDeclareView: changing expected traces to " + event.target.valueAsNumber)
        setActualExpectedTraces(() => event.target.valueAsNumber);
    }

    function onNrSegmentsChanged(event: React.ChangeEvent<HTMLInputElement>) {
        console.log("probDeclareView: changing nr segments to " + event.target.valueAsNumber)
        setNrSegments(() => event.target.valueAsNumber);
    }

    function onSegmentSizeChanged(event: React.ChangeEvent<HTMLInputElement>) {
        console.log("probDeclareView: changing segment size to " + event.target.valueAsNumber)
        setSegmentSize(() => event.target.valueAsNumber);
    }

    const showExistingModels = () => {
        setLoadingExistingModels(true);
        RestService.get<ProbDeclareInfo[]>(
            "/prob-declare/existing?source-file=" + sourceFile)
            .then((response) =>
                setExistingModels(() => response.data!))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoadingExistingModels(() => false));
    }

    const renderExistingModels = () => {
        return loadingExistingModels ? (renderLoadingIndicator()) : (existingModels && (
            <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} size="small" aria-label="existing models">
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Existing Models
                            </TableCell>
                        </TableRow>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Insert Date</TableCell>
                            <TableCell>Update Date</TableCell>
                            <TableCell>Generating</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {existingModels.map((row:ProbDeclareInfo) => (
                                <TableRow key={row.id} sx={{'&:last-child td, &:last-child th': {border: 0}}}>
                                    <TableCell component="th" scope="row">
                                        {row.id}
                                    </TableCell>
                                    <TableCell>
                                        {row.insertDate.toString()}
                                    </TableCell>
                                    <TableCell>
                                        {row.updateDate.toString()}
                                    </TableCell>
                                    <TableCell>
                                        {row.generating}
                                    </TableCell>
                                </TableRow>
                            ))}
                    </TableBody>
                </Table>
            </TableContainer>
        ));
    }

    const renderProbDeclare = () => {
        return probDeclare && (
            <TableContainer component={Paper}>
                <Table sx={{minWidth: 650}} size="small" aria-label="current model">
                    <TableHead>
                        <TableRow>
                            <TableCell>
                                Prob Declare Id: {probDeclare.id} (generating: {probDeclare.generating ? "true" : "false"})
                            </TableCell>
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
        );
    }

    const renderLoadingIndicator = () => {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                <CircularProgress/>
            </Box>
        );
    }

    return (
        <>
            <Box>
            <Typography variant="h4">Prob Declare Modell for {sourceFile}</Typography>
                <Box>
                    <Typography variant="h5">Existing Models</Typography>
                    <Button variant={'contained'} onClick={showExistingModels}>
                        show existing models
                    </Button>
                    {renderExistingModels()}
                </Box>
                <Box>
                    <Typography variant="h5">Options for Generation</Typography>
                    <Button variant={'contained'} onClick={initModelGeneration} disabled={aborting || initialized}>
                        start
                    </Button>
                    <Button variant={'contained'} onClick={pause}
                            disabled={paused || aborting || !probDeclare?.generating}>
                        pause
                    </Button>
                    <Button variant={'contained'} onClick={resume}
                            disabled={!paused || aborting || !probDeclare?.generating}>
                        resume
                    </Button>
                    <Button variant={'contained'} onClick={abort}
                            disabled={aborting /*|| !probDeclare?.generating*/}>
                        abort
                    </Button>
                    <Button variant={'contained'} onClick={close}
                            disabled={aborting || probDeclare?.generating && !paused}>
                        close
                    </Button>
                </Box>
                <Box>
                    <Typography variant="h5">Paging Options</Typography>
                    <TextField id={'start_page'} label={'Start Page (>=0)'} defaultValue={sourceDetails.page}
                               type={'number'}
                               variant={'standard'} onChange={onStartPageChanged} disabled={probDeclare !== null}/>
                    <TextField id={'page_size'} label={'Page Size'} defaultValue={sourceDetails.size} type={'number'}
                               variant={'standard'} onChange={onPageSizeChanged} disabled={probDeclare !== null}/>
                    <TextField id={'expected_traces'} label={'Expected Traces'} defaultValue={expectedTraces}
                               type={'number'}
                               variant={'standard'} onChange={onActualExpectedTracesChanged}
                               disabled={probDeclare !== null}/>
                </Box>
                <Box>
                    <Typography variant="h5">Segmentation (Evaluation only)</Typography>
                    <TextField id={'nr_segments'} label={'Nr Segments'} defaultValue={nrSegments} type={'number'}
                               variant={'standard'} onChange={onNrSegmentsChanged} disabled={probDeclare !== null}/>
                    <TextField id={'segment_size'} label={'Segment Size'} defaultValue={segmentSize} type={'number'}
                               variant={'standard'} onChange={onSegmentSizeChanged} disabled={probDeclare !== null}/>
                </Box>
                {loading && probDeclare === null ? (renderLoadingIndicator()) : (renderProbDeclare())}
            </Box>
            <JsonView data={probDeclare}/>
        </>
    );
};

export default ProbDeclareView;