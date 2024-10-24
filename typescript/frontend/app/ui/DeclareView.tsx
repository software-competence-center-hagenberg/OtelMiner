import {Component} from "react";
import {Accordion, AccordionDetails, AccordionSummary, Grid2} from "@mui/material";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

interface HashMap {
    [key: string]: string[]
}

interface Declare {
    constraint: string,
    values: string[]
}

interface DeclareViewProps {
    rawData: string;
}

interface DeclareViewState {
    model: Declare[][];
}

class DeclareView extends Component<DeclareViewProps, DeclareViewState> {
    constructor(props: DeclareViewProps) {
        super(props);
        this.state = this.initState();
    }

    private readonly initState = () => {
        const {rawData} = this.props;
        const model: Declare[][] = this.parseRawData(rawData).map((rd) => this.mapStringArrayToDeclareArray(rd));

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
    parseRawData = (rawData: string): string[][] => {
        return rawData.slice(3, -3)
            .split("], [")
            .map((innerArray) => innerArray.split(", ")); // FIXME parsing error constraints with 2 args are also splitted!
    }

    private readonly mapStringArrayToDeclareArray = (rd: string[]): Declare[] => {
        const declare: HashMap = {};

        rd.forEach((rawConstraint) => this.insertRawConstraintIntoHashMap(rawConstraint, declare));

        return Object.entries(declare).map(([constraint, values]) => ({
            constraint,
            values,
        }));
    }

    /**
     * <pre>
     *
     * </pre>
     * @param rawConstraint
     * @param hashMap
     */
    private readonly insertRawConstraintIntoHashMap = (rawConstraint: string, hashMap: HashMap) => {
        const regexConstraint = /([a-zA-Z_])*/;
        const regexValues = /\((.)*\)/;
        const matchConstraint = regexConstraint.exec(rawConstraint);
        const matchValues = regexValues.exec(rawConstraint); //FIXME

        if (matchConstraint && matchValues) {
            const constraint = matchConstraint[0];
            //const values = matchConstraint[2].split(',').map(value => `(${value.trim()})`);
            const values = [matchValues[0]];

            if (hashMap[constraint]) {
                hashMap[constraint] = Array.from(new Set([...hashMap[constraint], ...values]));
            } else {
                hashMap[constraint] = values;
            }
        }
    }


    render = () => {
        const {model} = this.state;

        return (
            <Grid2>
                {model.map((declareArray) => (
                    declareArray.map((declare) => (
                        <Accordion key={declare.constraint}>
                            <AccordionSummary expandIcon={<ExpandMoreIcon/>}>
                                <Typography variant="h6">{declare.constraint}</Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography>
                                    {declare.values.join(', ')}
                                </Typography>
                            </AccordionDetails>
                        </Accordion>
                    ))
                ))}
            </Grid2>
        );
    }
}

export default DeclareView;