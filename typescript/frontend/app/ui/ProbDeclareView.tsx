import React, {useMemo, useRef, useState} from "react";
import {
    Box,
    Button,
    CircularProgress,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextareaAutosize,
    TextField
} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Typography from "@mui/material/Typography";
import JsonView from "@/app/ui/json/JsonView";
import {AxiosResponse} from "axios";
import {defaultSourceDetails, SourceDetails} from "@/app/lib/Util";
import Statistics from "@/app/ui/Statistics";
import {DeclareConstraint, ProbDeclare, ProbDeclareInfo} from "@/app/lib/probDeclare";

interface ProbDeclareViewProps {
    sourceFile: string;
    expectedTraces: number;
    closeCallBack: () => void;
}

// @ts-ignore
function formatDateArray(date): string {
    const [year, month, day, hour, minute, second, nanoseconds] = date;

    return (day < 10 ? '0' : '') + day + '.' +
        (month < 10 ? '0' : '') + month + '.' +
        year + ' ' +
        (hour < 10 ? '0' : '') + hour + ':' +
        (minute < 10 ? '0' : '') + minute + ':' +
        (second < 10 ? '0' : '') + second + ':' +
        nanoseconds;
}

const ProbDeclareView = ({sourceFile, expectedTraces, closeCallBack}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState<boolean>(false);
    const [initialized, setInitialized] = useState<boolean>(false);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);
    const [aborting, setAborting] = useState<boolean>(false);
    const [sourceDetails, setSourceDetails] = useState<SourceDetails>(defaultSourceDetails(sourceFile));
    const [actualExpectedTraces, setActualExpectedTraces] = useState<number>(expectedTraces);
    const [nrSegments, setNrSegments] = useState<number>(0);
    const [segmentSize, setSegmentSize] = useState<number>(-1);
    const [existingModels, setExistingModels] = useState<ProbDeclareInfo[]>([]);
    const [loadingExistingModels, setLoadingExistingModels] = useState<boolean>(false);
    const [selectedProbDeclareInfo, setSelectedProbDeclareInfo] = useState<ProbDeclareInfo | null>(null);
    const [showGenerationOptions, setShowGenerationOptions] = useState<boolean>(false);
    const [showStatistics, setShowStatistics] = useState<boolean>(false);
    const [showSeedView, setShowSeedView] = useState<boolean>(false);
    const [seed, setSeed] = useState<string>("");
    const [nrTracesSeed, setNrTracesSeed] = useState<number>(1);
    const [calculatingSeededResult, setCalculatingSeededResult] = useState<boolean>(false);
    const [expectedSeededResult, setExpectedSeededResult] = useState<DeclareConstraint[]>([]);
    const [showExpectedSeededResult, setShowExpectedSeededResult] = useState<boolean>(false);
    const [seedingInProgress, setSeedingInProgress] = useState<boolean>(false);
    const [seedingResultReady, setSeedingResultReady] = useState<boolean>(false);
    const [showRawData, setShowRawData] = useState<boolean>(false);

    const timerRef = useRef<NodeJS.Timeout | null>(null);

    const setPaused = (paused: boolean) => {
        setProbDeclare((prev) => ({
            ...prev!,
                paused: paused
            }
        ));
    }

    const handleProbDeclareResponse = (response: AxiosResponse<any, ProbDeclare>) => {
        setInitialized(true);
        const probDeclare: ProbDeclare = response.data;
        setProbDeclare((prev) => (prev ? {
            ...prev,
            constraints: probDeclare.constraints, //.toSorted((a, b) => b.probability - a.probability),
            generating: probDeclare.generating,
            paused: probDeclare.paused,
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
            .then((response) => handleProbDeclareResponse(response))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(() => false));
    };

    const loadExistingModel = () => {
        setLoading(true);
        setProbDeclare(() => null);
        RestService.post<SourceDetails, ProbDeclare>("/prob-declare/load/"
            + selectedProbDeclareInfo?.id!
            + "?expected-traces=" + actualExpectedTraces
            + "&nr-segments=" + nrSegments
            + "&segment-size=" + segmentSize
            , sourceDetails)
            .then((response) => handleProbDeclareResponse(response))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(() => false));
    }

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
        if (probDeclare?.paused || aborting || !initialized) {
            return;
        }
        if (probDeclare?.generating) {
            timerRef.current = setTimeout(fetchModel, 2000);
        }
    }

    const reset = () => {
        if (probDeclare?.paused || probDeclare?.generating) {
            abort();
        } else {
            setAborting(false);
            clearTimer();
        }
        setInitialized(false);
        setProbDeclare(null);
        setLoading(false);
        setSourceDetails(defaultSourceDetails(sourceFile));
        setActualExpectedTraces(expectedTraces);
        setNrSegments(0);
        setSegmentSize(-1);
        setShowGenerationOptions(false);
        setShowStatistics(false);
        setShowSeedView(false);
        setSeed("");
        setNrTracesSeed(1);
        setCalculatingSeededResult(false);
        setExpectedSeededResult([]);
        setShowExpectedSeededResult(false);
        setSeedingInProgress(false);
        setSeedingResultReady(false);
    }

    useMemo(reset, [sourceFile])
    useMemo(updateModel, [probDeclare]);

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
                .then(() => {
                    setAborting(false);
                    reset();
                })
                .catch((error) => console.error('Error aborting generation of prob declare model', error));
        }
    }
    const close = () => {
        reset();
        closeCallBack();
    }

    function mapBoolToString(b: boolean) {
        return b ? "true" : "false";
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
            .then((response) => setExistingModels(() => response.data!))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoadingExistingModels(() => false));
    }
    const handleRowClick = (row: ProbDeclareInfo) => {
        console.log("row selected:");
        console.log(row);
        setSelectedProbDeclareInfo(() => row);
    };


    function onSeedChanged(event: React.ChangeEvent<HTMLTextAreaElement>) {
        console.log("probDeclareView: changing seed to " + event.target.value)
        setSeed(() => event.target.value);
    }

    function onNrTracesSeedChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setNrTracesSeed(event.target.valueAsNumber);
    }

    const addDeclare = (clone: DeclareConstraint[], cloneTracesProcessed: number, declareTemplate: string) => {
        let found = false;
        clone.forEach(c => {
            if (c.declareTemplate === declareTemplate) {
                c.nr += nrTracesSeed;
                found = true;
            }
            c.probability = c.nr / cloneTracesProcessed;
        })
        if (!found) {
            clone.push({declareTemplate, nr: nrTracesSeed, probability: nrTracesSeed / cloneTracesProcessed})
        }
    }

    const calculateExpectedSeededResult = () => {
        if (!probDeclare) {
            console.error("no prob declare model present");
            return;
        }
        setCalculatingSeededResult(true);
        setExpectedSeededResult([]);
        if (!probDeclare.paused && probDeclare.generating) {
            console.error("generation active -> pause or finish first!");
            return;
        }
        const s = seed.trim();
        let clone: DeclareConstraint[] = probDeclare?.constraints.map(c => ({...c}));
        let cloneTracesProcessed = probDeclare.tracesProcessed;
        cloneTracesProcessed += nrTracesSeed;
        if (s.startsWith("[")) {
            if (!s.endsWith("]")) {
                console.error("error in seed array");
                setCalculatingSeededResult(false);
                return;
            }
            const declareTemplatesToAdd = s.slice(1, -1).split(",").map(d => d.trim());
            declareTemplatesToAdd.forEach(dt => addDeclare(clone, cloneTracesProcessed, dt));
        } else {
            addDeclare(clone, cloneTracesProcessed, s);
        }

        setExpectedSeededResult(clone);
        setCalculatingSeededResult(false);
    }

    const initSeeding = () => {
        // TODO implement
        const s = seed.trim();
        // send seed to backend and start polling again in callback
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
                            <TableRow key={row.id} sx={{'&:last-child td, &:last-child th': {border: 0}}}
                                      onClick={() => handleRowClick(row)}>
                                    <TableCell component="th" scope="row">
                                        {row.id}
                                    </TableCell>
                                    <TableCell>
                                        {formatDateArray(row.insertDate)}
                                    </TableCell>
                                    <TableCell>
                                        {row.updateDate.toString()}
                                    </TableCell>
                                    <TableCell>
                                        {mapBoolToString(row.generating)}
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
                                Prob Declare Id: {probDeclare.id} (generating: {mapBoolToString(probDeclare.generating)})
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
                    <Button variant={'contained'} onClick={loadExistingModel}
                            disabled={selectedProbDeclareInfo === null}>
                        load existing model
                    </Button>
                    {renderExistingModels()}
                </Box>
                <Box>
                    <Typography variant="h5">Options for Generation</Typography>
                    <Button variant={'contained'} onClick={initModelGeneration} disabled={aborting || initialized}>
                        start
                    </Button>
                    <Button variant={'contained'} onClick={pause}
                            disabled={probDeclare?.paused || aborting || !probDeclare?.generating}>
                        pause
                    </Button>
                    <Button variant={'contained'} onClick={resume}
                            disabled={!probDeclare?.paused || aborting || !probDeclare?.generating}>
                        resume
                    </Button>
                    <Button variant={'contained'} onClick={abort}
                            disabled={aborting || !initialized/*|| !probDeclare?.generating*/}>
                        abort
                    </Button>
                    <Button variant={'contained'} onClick={reset}>
                        reset
                    </Button>
                    <Button variant={'contained'} onClick={close}
                            disabled={aborting || probDeclare?.generating && !probDeclare?.paused}>
                        close
                    </Button>
                </Box>
                <Button variant={'contained'} onClick={() => setShowGenerationOptions(!showGenerationOptions)}>
                    show generation options
                </Button>
                {showGenerationOptions && (
                    <>
                        <Box>
                            <Typography variant="h5">Paging Options</Typography>
                            <TextField id={'start_page'} label={'Start Page (>=0)'} defaultValue={sourceDetails.page}
                                       type={'number'} variant={'standard'} onChange={onStartPageChanged}
                                       disabled={probDeclare !== null}/>
                            <TextField id={'page_size'} label={'Page Size'} defaultValue={sourceDetails.size}
                                       type={'number'} variant={'standard'} onChange={onPageSizeChanged}
                                       disabled={probDeclare !== null}/>
                            <TextField id={'expected_traces'} label={'Expected Traces'} defaultValue={expectedTraces}
                                       type={'number'} variant={'standard'} onChange={onActualExpectedTracesChanged}
                                       disabled={probDeclare !== null}/>

                        </Box>
                        <Box>
                            <Typography variant="h5">Segmentation (Evaluation only)</Typography>
                            <TextField id={'nr_segments'} label={'Nr Segments'} defaultValue={nrSegments}
                                       type={'number'} variant={'standard'} onChange={onNrSegmentsChanged}
                                       disabled={probDeclare !== null}/>
                            <TextField id={'segment_size'} label={'Segment Size'} defaultValue={segmentSize}
                                       type={'number'} variant={'standard'} onChange={onSegmentSizeChanged}
                                       disabled={probDeclare !== null}/>
                        </Box>
                    </>
                )}
                <Button variant={'contained'} onClick={() => setShowStatistics(!showStatistics)}>
                    show statistics
                </Button>
                {showStatistics &&
                    <Box>
                        <Typography variant="h5">Statistics</Typography>
                        {probDeclare?.constraints! && <Statistics constraints={probDeclare.constraints}/>}
                    </Box>}
                <Button variant={'contained'} onClick={() => setShowSeedView(!showSeedView)}>
                    show seeding options
                </Button>
                {showSeedView &&
                    <Box>
                        <Typography variant="h5">Seed Model</Typography>
                        <TextareaAutosize
                            aria-label="minimum height"
                            minRows={5}
                            placeholder="Enter Declare Constraint(s)"
                            style={{width: 255}}
                            onChange={onSeedChanged}
                            disabled={calculatingSeededResult}
                        />
                        <TextField id={'nr-traces-seed'} label={'Nr. Traces'} defaultValue={nrTracesSeed}
                                   type={'number'} variant={'standard'} onChange={onNrTracesSeedChanged}
                                   disabled={calculatingSeededResult}/>
                        <Button variant={'contained'} onClick={calculateExpectedSeededResult}
                                disabled={calculatingSeededResult}>
                            calculate expected result
                        </Button>
                        <Button variant={'contained'}
                                onClick={() => setShowExpectedSeededResult(!showExpectedSeededResult)}>
                            show expected seeded result
                        </Button>
                        {showExpectedSeededResult && <JsonView data={expectedSeededResult}/>}
                        <Button variant={'contained'} onClick={initSeeding}
                                disabled={expectedSeededResult.length === 0}>
                            seed
                        </Button>
                        {seedingResultReady && <Typography>
                            Expected === Actual:
                            {expectedSeededResult
                                .toSorted((a, b) => b.probability - a.probability)
                            === probDeclare?.constraints!
                                .toSorted((a, b) => b.probability - a.probability)
                                ? "true" : "false"}
                        </Typography>}
                    </Box>}
                {loading && probDeclare === null ? (renderLoadingIndicator()) : (renderProbDeclare())}
            </Box>
            <Button variant={'contained'} onClick={() => setShowRawData(!showRawData)}>
                show raw data
            </Button>
            {showRawData && <JsonView data={probDeclare}/>}
        </>
    );
};

export default ProbDeclareView;