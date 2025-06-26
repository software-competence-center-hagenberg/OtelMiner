import React, {useMemo, useRef, useState} from "react";
import {
    Box,
    Button,
    CircularProgress, Grid2,
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
import {ProbDeclare, ProbDeclareConstraint, ProbDeclareInfo} from "@/app/lib/probDeclare";
import DeclareView from "@/app/ui/DeclareView";
import {sort} from "next/dist/build/webpack/loaders/css-loader/src/utils";

interface ProbDeclareViewProps {
    sourceFile: string;
    expectedTraces: number;
    closeCallBack: () => void;
}

interface SeedTraceDetails {
    traceId: string;
    nrNodes: number;
    spans: string[];
    traceDataType: string;
}

interface Seed {
    traceData: SeedTraceDetails;
    nrTraces: number;
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
    const [seed, setSeed] = useState<Seed | undefined>(undefined);
    const [seedSpans, setSeedSpans] = useState<string>("");
    const [seedNrTraces, setSeedNrTraces] = useState<number>(1);
    const [seedTraceId, setSeedTraceId] = useState<string>("");
    const [calculatingSeededResult, setCalculatingSeededResult] = useState<boolean>(false);
    const [expectedSeededResult, setExpectedSeededResult] = useState<ProbDeclareConstraint[]>([]);
    const [showExpectedSeededResult, setShowExpectedSeededResult] = useState<boolean>(false);
    const [seedingInProgress, setSeedingInProgress] = useState<boolean>(false);
    const [seedingResultReady, setSeedingResultReady] = useState<boolean>(false);
    const [calculatingSeededDeclareModel, setCalculatingSeededDeclareModel] = useState<boolean>(false);
    const [seedingEvaluationSuccessful, setSeedingEvaluationSuccessful] = useState<boolean>(false);
    const [seededDeclareModel, setSeededDeclareModel] = useState<string[] | undefined>(undefined);
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
            constraints: probDeclare.constraints,
            generating: probDeclare.generating,
            paused: probDeclare.paused,
            tracesProcessed: probDeclare.tracesProcessed
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
        setSeed(undefined);
        setSeedSpans("");
        setSeedNrTraces(1);
        setSeedTraceId("");
        setCalculatingSeededResult(false);
        setExpectedSeededResult([]);
        setShowExpectedSeededResult(false);
        setSeedingInProgress(false);
        setSeedingResultReady(false);
        setCalculatingSeededDeclareModel(false);
        setSeedingEvaluationSuccessful(false);
        setSeededDeclareModel(undefined);
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
            .catch((error) => console.error('Error resuming generation of prob declare model', error))
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
        setSourceDetails(prev => ({
            ...prev,
            page: event.target.valueAsNumber
        }));
    }

    function onPageSizeChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setSourceDetails(prev => ({
            ...prev,
            size: event.target.valueAsNumber
        }));
    }

    function onActualExpectedTracesChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setActualExpectedTraces(() => event.target.valueAsNumber);
    }

    function onNrSegmentsChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setNrSegments(() => event.target.valueAsNumber);
    }

    function onSegmentSizeChanged(event: React.ChangeEvent<HTMLInputElement>) {
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


    function onSeedSpansChanged(event: React.ChangeEvent<HTMLTextAreaElement>) {
        setSeedSpans(() => event.target.value);
    }

    function onSeedNrTracesChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setSeedNrTraces(event.target.valueAsNumber);
    }

    function onSeedTraceIdChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setSeedTraceId(event.target.value);
    }

    const addDeclare = (clone: ProbDeclareConstraint[], cloneTracesProcessed: number, declareTemplate: string) => {
        let found = false;
        clone.forEach(c => {
            if (c.declareTemplate === declareTemplate) {
                c.nr += seedNrTraces;
                found = true;
            }
            c.probability = c.nr / cloneTracesProcessed;
        })
        if (!found) {
            clone.push({declareTemplate, nr: seedNrTraces, probability: seedNrTraces / cloneTracesProcessed})
        }
    }

    const calculateExpectedSeededResult = () => {
        if (!probDeclare || !seededDeclareModel) {
            console.error("no prob declare or seeded declare model present");
            return;
        }
        setCalculatingSeededResult(true);
        setExpectedSeededResult([]);
        if (!probDeclare.paused && probDeclare.generating) {
            console.error("generation active -> pause or finish first!");
            return;
        }
        let clone: ProbDeclareConstraint[] = probDeclare?.constraints.map(c => ({...c}));
        let cloneTracesProcessed = probDeclare.tracesProcessed;
        cloneTracesProcessed += seedNrTraces;
        seededDeclareModel.forEach(dt => addDeclare(clone, cloneTracesProcessed, dt));

        setExpectedSeededResult(clone);
        setCalculatingSeededResult(false);
    }

    const initSeeding = () => {
        if (!seed) {
            console.error("no seed present!");
            return;
        }
        setSeedingInProgress(true)
        RestService.post<Seed, ProbDeclare>("/prob-declare/seed/" + probDeclare?.id!, seed)
            .then((_response) => resume())
            .catch((error) => console.error('Error seeding', error))
            .finally(() => setSeedingInProgress(false));
    }

    const evaluateSeedingResult = () => {
        const sortFn = (a:ProbDeclareConstraint, b:ProbDeclareConstraint) => b.declareTemplate.localeCompare(a.declareTemplate);
        const expected = expectedSeededResult.toSorted(sortFn);
        const actual = probDeclare!.constraints!.toSorted(sortFn);
        if (expected.length !== actual.length) {
            setSeedingEvaluationSuccessful(false);
            return;
        }

        const diff: {actual: ProbDeclareConstraint, expected: ProbDeclareConstraint}[] = [];
        for (let i = 0; i < expected.length; i++) {
            if (expected[i].probability !== actual[i].probability
                || expected[i].nr !== actual[i].nr
                || expected[i].declareTemplate !== actual[i].declareTemplate) {
                // console.log("expected != actual! constraint nr. " + i + ":");
                // console.log("expected:");
                // console.log(expected[i]);
                // console.log("actual:");
                // console.log(actual[i]);
                diff.push({expected: expected[i], actual: actual[i]})
            }
        }
        console.log("diff:");
        console.log(diff);
        setSeedingEvaluationSuccessful(diff.length === 0);
    }


    const pollSeedDeclareModel = async (traceId: string): Promise<string> => {
        return new Promise((resolve, reject) => {
            const intervalId = setInterval(async () => {
                try {
                    const response = await RestService.get<string>("/declare/" + traceId);
                    if (response.data !== '') {
                        clearInterval(intervalId);
                        console.log(response.data);
                        resolve(response.data);
                    }
                } catch (error) {
                    console.error('Error polling model:', error);
                    clearInterval(intervalId)
                    reject(new Error("Error polling model"));
                }
            }, 2000);
        });
    };

    const onClickGenerateDeclareModelForSeed = async () => {
        if (!seedSpans) {
            console.error("no seed spans present!");
            return;
        }
        if (seedTraceId.length === 0) {
            console.error("no trace Id present!");
            return;
        }
        let s = seedSpans;
        if (s.startsWith("[")) {
            s = s.substring(1);
            if (s.endsWith("]")) {
                s = s.substring(0, s.length - 1);
            }
        }
        let spanList = s.replace(/\s+/g, '').split("},");
        for (let i = 0; i < spanList.length - 1; i++) {
            spanList[i] += "}"
        }
        let seedTraceDetails: SeedTraceDetails = {
            traceId: seedTraceId,
            nrNodes: spanList.length,
            spans: spanList,
            traceDataType: "DYNATRACE_SPANS_LIST"
        }

        setSeed(() => ({
            traceData: seedTraceDetails,
            nrTraces: seedNrTraces
        }));

        try {
            setCalculatingSeededDeclareModel(true)
            const response = await RestService.post<SeedTraceDetails, string>("/declare/generate", seedTraceDetails);
            const model = await pollSeedDeclareModel(response.data);
            const seededDeclareModel: string[] = JSON.parse(JSON.stringify(model));
            setSeededDeclareModel(seededDeclareModel);
        } catch (error) {
            console.error('Error generating model:', error);
        } finally {
            setCalculatingSeededDeclareModel(false);
        }
    };

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

    const renderSeedView = () => {
        return (
            <Box>
                <Box>
                    <Typography variant="h5">Seed Model</Typography>
                    <TextField id={'nr-traces-seed'} label={'Nr. Traces'} defaultValue={seedNrTraces}
                               type={'number'} variant={'standard'} onChange={onSeedNrTracesChanged}
                               disabled={calculatingSeededResult}/>
                    <TextField id={'trace-id-seed'} label={'Trace Id'} defaultValue={seedTraceId}
                               variant={'standard'} onChange={onSeedTraceIdChanged}
                               disabled={calculatingSeededResult}/>
                    <Button variant={"contained"} onClick={onClickGenerateDeclareModelForSeed}>
                        generate declare Model
                    </Button>
                </Box>
                <Grid2 container columns={2}>
                    <TextareaAutosize
                        aria-label="minimum height"
                        minRows={20}
                        placeholder="Enter Spans List"
                        style={{width: "50%"}}
                        onChange={onSeedSpansChanged}
                        disabled={calculatingSeededResult}
                    />
                    <Box width={"50%"}>
                        {seededDeclareModel && <DeclareView rawData={seededDeclareModel}/>}
                    </Box>
                </Grid2>
                <Box>
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
                    </Button> {/*TODO move up*/}
                    <Button variant={'contained'} onClick={evaluateSeedingResult}
                            disabled={expectedSeededResult.length === 0}>
                        evaluate seeding result
                    </Button> {/*TODO move up*/}
                    <Typography>seeding evaluation result: {seedingEvaluationSuccessful ? "SUCCESS" : "FAILURE"}</Typography>
                    {seedingResultReady && <Typography>
                        Expected === Actual:
                        {expectedSeededResult
                            .toSorted((a, b) => b.probability - a.probability)
                        === probDeclare?.constraints!
                            .toSorted((a, b) => b.probability - a.probability)
                            ? "true" : "false"}
                    </Typography>}
                </Box>
            </Box>
        )
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
                {showSeedView && renderSeedView()}
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
