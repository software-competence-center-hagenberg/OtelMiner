import React, { useState, useEffect } from 'react';
import { Accordion, AccordionDetails, AccordionSummary, Grid2, List, ListItem } from "@mui/material";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Box from "@mui/material/Box";

interface DeclareViewProps {
    rawData: string;
}

function parseRawData (rawData: string): Map<string, string[]> {
    const model = new Map<string, string[]>();
    const regex = /([A-Z_])*\([a-zA-Z ,/.]*\)/gm;
    const matches = rawData.match(regex);
    if (matches) {
        matches.forEach((match) => {
            const [key, value] = match.split('(');
            const valueWithoutParenthesis = value.slice(0, -1);
            if (model.has(key)) {
                model.get(key)?.push(valueWithoutParenthesis);
            } else {
                model.set(key, [valueWithoutParenthesis]);
            }
        });
    }
    return model;
}

const DeclareView = ({ rawData }: DeclareViewProps) => {
    const [model, setModel] = useState<Map<string, string[]>>(new Map<string, string[]>());

    useEffect(() => {
        const parsedModel = parseRawData(rawData);
        setModel(parsedModel);
    }, [rawData]);

    return (
        <Box>
            {Array.from(model.entries()).map(([key, values]) => (
                <Accordion key={key}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6">{key}</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <List>
                            {values.map((value, index) => (
                                <ListItem key={`${key}-${index}-${value}`}>{value}</ListItem>
                            ))}
                        </List>
                    </AccordionDetails>
                </Accordion>
            ))}
        </Box>
    );
}

export default DeclareView;