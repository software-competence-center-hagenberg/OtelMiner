import React, {useEffect, useState} from 'react';
import {CircularProgress, Grid2} from "@mui/material";
import RestService from "@/app/lib/RestService";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import DeclareView from "@/app/ui/DeclareView";

interface ProbDeclareViewProps {
    sourceFile: string;
}

interface DeclareConstraint {
    probability: number;
    template: string;
}

interface ProbDeclare {
    id: string;
    constraints: DeclareConstraint[];
    generating: boolean;
}

const ProbDeclareView = ({sourceFile}: ProbDeclareViewProps) => {
    const [loading, setLoading] = useState(true);
    const [probDeclare, setProbDeclare] = useState<ProbDeclare | null>(null);

    useEffect(() => {
        fetchSourceDetails();
    }, [sourceFile]);

    const fetchSourceDetails = () => {
        setLoading(true);
        RestService.post<string, ProbDeclare>("/generate-prob-declare-model", sourceFile!)
            .then((response) => {
                const probDeclare: ProbDeclare = response.data;
                setProbDeclare(probDeclare);
                if (probDeclare.generating) {
                    setTimeout(fetchSourceDetails, 500);
                }
            })
            .catch((error) => console.error('Error fetching prob declare model', error))
            .finally(() => setLoading(false));
    };

    const pollModel = async (traceId: string): Promise<string> => {
        try {
            const response = await RestService.get<string>("/model/" + traceId);
            if (response.data !== '') {
                return response.data;
            } else {
                return await pollModel(traceId);
            }
        } catch (error) {
            console.error('Error polling model:', error);
            throw error;
        }
    };

    return (
        <Box>
            <Typography variant="h4">Trace Details</Typography>
            {loading && !probDeclare ? (
                <Box display="flex" justifyContent="center" alignItems="center" height="100%">
                    <CircularProgress/>
                </Box>
            ) : (
                probDeclare && (
                    <Grid2 size={6} columns={6}>
                        <DeclareView rawData={
                            probDeclare.constraints
                                .filter(c => c.probability === 1)
                                .map(c => c.template)
                        }/>
                        <DeclareView rawData={
                            probDeclare.constraints
                                .filter(c => c.probability < 1)
                                .map(c => c.template)
                        }/>
                    </Grid2>
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