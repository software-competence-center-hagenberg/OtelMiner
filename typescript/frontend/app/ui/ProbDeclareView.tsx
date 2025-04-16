import React, {useEffect, useMemo, useState} from 'react';
import {Button, CircularProgress} from "@mui/material";
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
    template: string;
}

interface ProbDeclare {
    id: string;
    generating: boolean;
    constraints: DeclareConstraint[];
    traces: string[];
}

const ProbDeclareView = ({sourceFile, expectedTraces, abortCallback}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState<boolean>(true);
    const [initialized, setInitialized] = useState<boolean>(false);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);
    const [aborting, setAborting] = useState<boolean>(false);
    const [paused, setPaused] = useState<boolean>(false);

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

    const fetchModel = async  () => {
        try {
            const response = await RestService.get<ProbDeclare>("/prob-declare/" + probDeclare!.id);
            handleProbDeclareResponse(response);
            // updateModel();
        } catch (error) {
            console.error('Error fetching prob declare model', error);
        }
    }

    const updateModel = () => {
        if (paused || aborting) {
            return;
        }
        if (!initialized) {
            initModelGeneration()
        } else if (probDeclare?.generating) {
            setTimeout(fetchModel, 2000);
        }
    }

    useMemo(updateModel, [sourceFile, probDeclare?.constraints, probDeclare?.generating]);

    const abort = () => {
        setAborting(true);
        if (initialized) {
            console.debug("model initialized --> sending abort request to backend.")
            RestService.get<void>("/prob-declare/abort/" + probDeclare?.id)
                .then((_) => abortCallback())
                .catch((error) => console.error('Error aborting generation of prob declare model', error));
        } else {
            console.debug("model NOT initialized --> aborting in callback.")
            // abortCallback();
        }
    }

    const pause = () => {
        console.debug("model initialized --> sending pause request.")
        RestService.get<void>("/prob-declare/pause/" + probDeclare!.id)
            .then((_) => setPaused(true))
            .catch((error) => console.error('Error pausing generation of prob declare model', error));
    }

    const resume = () => {
        console.debug("model initialized --> sending resume request.")
        RestService.get<void>("/prob-declare/resume/" + probDeclare!.id)
            .then((_) => {
                setPaused(() => false);
            })
            .catch((error) => console.error('Error pausing generation of prob declare model', error))
            .finally(fetchModel);
    }

    return (
        <Box>
            <Typography variant="h4">Prob Declare Modell for {sourceFile}</Typography>
            {loading && !probDeclare ? (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <CircularProgress/>
                </Box>
            ) : (
                probDeclare && (
                    <JsonView data={probDeclare}/>
                    // <Grid2 size={6} columns={6}>
                    //     <DeclareView rawData={
                    //         probDeclare.constraints
                    //             .filter(c => c.probability === 1)
                    //             .map(c => c.template)
                    //     }/>
                    //     <DeclareView rawData={
                    //         probDeclare.constraints
                    //             .filter(c => c.probability < 1)
                    //             .map(c => c.template)
                    //     }/>
                    // </Grid2>
                )
            )}
            <Button
                variant={'contained'}
                onClick={abort}
                disabled={aborting}
            >
                abort
            </Button>
            <Button
                variant={'contained'}
                onClick={pause}
                disabled={paused || aborting}
            >
                pause
            </Button>
            <Button
                variant={'contained'}
                onClick={resume}
                disabled={!paused || aborting}
            >
                resume
            </Button>
        </Box>
    );
};

export default ProbDeclareView;