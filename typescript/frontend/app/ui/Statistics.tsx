import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts";
import React, { useMemo, useState } from "react";
import { ProbDeclareConstraint } from "@/app/lib/probDeclare";
import { Box, TextField } from "@mui/material";
import Typography from "@mui/material/Typography";

export interface MyValue {
    name: string,
    value: number,
}

export interface StatisticsProps {
    constraints: ProbDeclareConstraint[]
}

function createEmptyThreshholdList() {
    return [
        { name: "[0.0, 0.1[", value: 0 },
        { name: "[0.1, 0.2[", value: 0 },
        { name: "[0.2, 0.3[", value: 0 },
        { name: "[0.3, 0.4[", value: 0 },
        { name: "[0.4, 0.5[", value: 0 },
        { name: "[0.5, 0.6[", value: 0 },
        { name: "[0.6, 0.7[", value: 0 },
        { name: "[0.7, 0.8[", value: 0 },
        { name: "[0.8, 0.9[", value: 0 },
        { name: "[0.9, 1[", value: 0 },
        { name: "= 1", value: 0 },
    ]
}

const ranges = [
    { min: 0.0, max: 0.1, index: 0 },
    { min: 0.1, max: 0.2, index: 1 },
    { min: 0.2, max: 0.3, index: 2 },
    { min: 0.3, max: 0.4, index: 3 },
    { min: 0.4, max: 0.5, index: 4 },
    { min: 0.5, max: 0.6, index: 5 },
    { min: 0.6, max: 0.7, index: 6 },
    { min: 0.7, max: 0.8, index: 7 },
    { min: 0.8, max: 0.9, index: 8 },
    { min: 0.9, max: 1, index: 9 },
];

function mapConstraintsByNrBundled(constraints: ProbDeclareConstraint[]) {
    const values: MyValue[] = createEmptyThreshholdList();
    constraints.forEach(c => {
        ranges.forEach(range => {
            if (c.probability >= range.min && c.probability < range.max) {
                ++values[range.index].value;
            } else if (c.probability === 1) {
                ++values[10].value;
            }
        })
    });
    return values;
}

function mapConstraintsByProbUnderThreshold(constraints: ProbDeclareConstraint[], threshold: number) {
    return constraints
        .filter(x => x.probability >= threshold)
        .toSorted((a, b) => b.probability - a.probability);
}

const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload?.length) {
        console.log("Payload:");
        console.log(payload);
        return (
            <div className="bg-white p-3 border border-gray-300 rounded shadow-lg">
                <p className="font-medium">{`Template: ${label}`}</p>
                <p className="text-blue-600">{`Count: ${payload[0].payload.nr}`}</p>
                <p className="text-blue-600">{`Probability: ${payload[0].payload.probability}`}</p>
            </div>
        );
    }
    return null;
};

const truncateLabel = (label: string, maxLength: number = 25) => {
    if (label.length <= maxLength) return label;
    return '' + label + ''.substring(0, maxLength) + '...';
};

const CustomXAxisTick = (props: any) => {
    const { x, y, payload } = props;
    const truncatedLabel = truncateLabel(payload.value, 25);

    return (
        <g transform={`translate(${x},${y})`}>
            <text
                x={0}
                y={0}
                dy={16}
                textAnchor={"end"}
                fill={"#666"}
                fontSize={"12"}
                transform={"rotate(-45)"}
            >
                {truncatedLabel}
            </text>
        </g>
    );
};


const Statistics = ({ constraints }: StatisticsProps) => {
    const [threshold, setThreshold] = useState<number>(0.1);
    const constraintsByNrBundled = useMemo<MyValue[]>(() => mapConstraintsByNrBundled(constraints), [constraints])
    const constraintsByProbGreaterEqualThreshold = useMemo<ProbDeclareConstraint[]>(() => mapConstraintsByProbUnderThreshold(constraints, threshold), [constraints, threshold])

    function onThresholdChanged(event: React.ChangeEvent<HTMLInputElement>) {
        setThreshold(() => event.target.valueAsNumber);
    }

    return (
        <Box>
            <Typography variant="h6">All Traces by Probability</Typography>
            <LineChart width={1200} height={500} data={constraints}
                margin={{ top: 20, right: 30, left: 20, bottom: 120 }}
            >
                <CartesianGrid strokeDasharray={"3 3"} />
                <XAxis
                    dataKey={"declareTemplate"}
                    angle={-45}
                    textAnchor={"end"}
                    height={80}
                    interval={"preserveStartEnd"}
                    tick={<CustomXAxisTick />}
                />
                <YAxis dataKey={"probability"} />
                <Tooltip wrapperStyle={{ width: 100, backgroundColor: '#ccc' }} content={<CustomTooltip />} />
                <Legend width={100} wrapperStyle={{ top: 40, right: 20, backgroundColor: '#f5f5f5', border: '1px solid #d5d5d5', borderRadius: 3, lineHeight: '40px' }} />
                <Line type="monotone" dataKey={"probability"} fill={"#8884d8"} name={"Probability"} />
            </LineChart>
            <Typography variant="h6">All Traces with Probability {">="} Threshold</Typography>
            <TextField id={'threshold'} label={'Threshold'} defaultValue={threshold}
                type={'number'} variant={'standard'} onChange={onThresholdChanged}
            />
            <LineChart width={1200} height={500} data={constraintsByProbGreaterEqualThreshold}
                margin={{ top: 20, right: 30, left: 20, bottom: 120 }}
            >
                <CartesianGrid strokeDasharray={"3 3"} />
                <XAxis
                    dataKey={"declareTemplate"}
                    angle={-45}
                    textAnchor={"end"}
                    height={80}
                    interval={"preserveStartEnd"}
                    tick={<CustomXAxisTick />}
                />
                <YAxis dataKey={"probability"} />
                <Tooltip wrapperStyle={{ width: 100, backgroundColor: '#ccc' }} content={<CustomTooltip />} />
                <Legend width={100} wrapperStyle={{ top: 40, right: 20, backgroundColor: '#f5f5f5', border: '1px solid #d5d5d5', borderRadius: 3, lineHeight: '40px' }} />
                <Line type="monotone" dataKey={"probability"} fill={"#8884d8"} name={"Probability"} />
            </LineChart>
            <Typography variant={"h6"}>Nr by Probability Buckets</Typography>
            <BarChart width={1200} height={250} data={constraintsByNrBundled}>
                <CartesianGrid strokeDasharray={"3 3"} />
                <XAxis dataKey={"name"} />
                <YAxis dataKey={"value"} />
                <Tooltip wrapperStyle={{ width: 100, backgroundColor: '#ccc' }} />
                <Legend width={100} wrapperStyle={{ top: 40, right: 20, backgroundColor: '#f5f5f5', border: '1px solid #d5d5d5', borderRadius: 3, lineHeight: '40px' }} />
                <Bar dataKey={"value"} fill={"#8884d8"} name={"Count"} />
            </BarChart>
        </Box>
    );
}

export default Statistics;