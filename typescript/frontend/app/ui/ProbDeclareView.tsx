import React, {useMemo, useState} from 'react';
import {Button, CircularProgress} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import JsonView from "@/app/ui/json/JsonView";
import {AxiosResponse} from "axios";
import {defaultSourceDetails, SourceDetails} from "@/app/lib/Util";

interface ProbDeclareViewProps {
    sourceFile: string;
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

const ProbDeclareView = ({sourceFile, abortCallback}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState(true);
    const [initialized, setInitialized] = useState(false)
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);

    const handleProbDeclareResponse = (response: AxiosResponse<any, ProbDeclare>) => {
        const probDeclare: ProbDeclare = response.data;
        setProbDeclare(() => probDeclare);
    }

    const initModelGeneration = () => {
        setLoading(() => true);
        setInitialized(() => true)
        const sourceDetails: SourceDetails = defaultSourceDetails(sourceFile);
        RestService.post<SourceDetails, ProbDeclare>("/prob-declare/generate", sourceDetails)
            .then((response) => handleProbDeclareResponse(response))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(() => false));
    };

    const fetchModel = async  () => {
        try {
            const response = await RestService.get<ProbDeclare>("/prob-declare/" + probDeclare!.id);
            handleProbDeclareResponse(response);
        } catch (error) {
            console.error('Error fetching prob declare model', error);
        }
    }

    const updateModel = () => {
        if (!initialized) {
            initModelGeneration()
        } else if (probDeclare?.generating) {
            setTimeout(fetchModel, 2000);
        }
    }

    const abort = () => {
        if (initialized) {
            console.debug("model initialized --> sending abort request to backend.")
            const sourceDetails: SourceDetails = defaultSourceDetails(sourceFile);
            RestService.post<SourceDetails, void>("/prob-declare/abort", sourceDetails)
                .then((_) => abortCallback())
                .catch((error) => console.error('Error aborting generation of prob declare model', error))
                .finally(() => setLoading(() => false));
        } else {
            console.debug("model NOT initialized --> calling abort callback immediately.")
            abortCallback();
        }
    }

    useMemo(updateModel, [sourceFile, probDeclare]);

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
                /*

                        {selectedRowModel && (
                            <Grid2 size={6} columns={6}>
                                <DeclareView rawData={selectedRowModel}/>
                            </Grid2>
                        )}
                 */
            )}
            <Button
                variant={'contained'}
                onClick={abort}
                // disabled={!sourceFile || !generatingProbDeclare}
            >
                abort
            </Button>
        </Box>
    );
};

export default ProbDeclareView;