import {Component} from "react";
import {Accordion, AccordionDetails, AccordionSummary, Grid2, List, ListItem} from "@mui/material";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

interface Declare {
    constraint: string,
    values: string[]
}

interface DeclareViewProps {
    rawData: string;
}

interface DeclareViewState {
    model: Map<string, string[]>;
}

class DeclareView extends Component<DeclareViewProps, DeclareViewState> {
    constructor(props: DeclareViewProps) {
        super(props);
        this.state = this.initState();
    }

    private readonly initState = () => {
        const {rawData} = this.props;
        const model: Map<string, string[]> = this.parseRawData(rawData);

        return {
            model: model,
        };
    }

    /**
     * <pre>
     *     Removes the outer square brackets (input.slice(3, -3).
     *     Then splits all inner arrays divided by "], [".
     *     Finally transforms the strings representing the inner arrays into actual string arrays.
     * </pre>
     * @param rawData string representation if DECLARE Constraint set
     */
    parseRawData = (rawData: string): Map<string, string[]> => {
        const model = new Map<string, string[]>();
        const regex = /([A-Z_])*\([a-zA-Z ,\/\.]*\)/gm;
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

    render = () => {
        const { model } = this.state;

        return (
            <Grid2>
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
            </Grid2>
        );
    };

}

export default DeclareView;