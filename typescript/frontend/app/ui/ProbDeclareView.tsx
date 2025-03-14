import React, {useEffect, useState} from 'react';
import {CircularProgress} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import JsonView from "@/app/ui/json/JsonView";
import {AxiosResponse} from "axios";
import {defaultSourceDetails, SourceDetails} from "@/app/lib/Util";

interface ProbDeclareViewProps {
    sourceFile: string;
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

const ProbDeclareView = ({sourceFile}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState(true);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);

    const handleProbDeclareResponse = (response: AxiosResponse<any, ProbDeclare>) => {
        const probDeclare: ProbDeclare = response.data;
        setProbDeclare(() => probDeclare);
        if (probDeclare.generating) {
            setTimeout(updateModel, 500);
        }
    }

    const initModelGeneration = () => {
        setLoading(() => true);
        const sourceDetails: SourceDetails = defaultSourceDetails(sourceFile);
        RestService.post<SourceDetails, ProbDeclare>("/prob-declare/generate", sourceDetails)
            .then((response) => handleProbDeclareResponse(response))
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(false));
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
        probDeclare !== null ? fetchModel() : initModelGeneration();
    }

    useEffect(updateModel, [sourceFile]);

    return (
        <Box>
            <Typography variant="h4">Trace Details</Typography>
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
        </Box>
    );
};

export default ProbDeclareView;