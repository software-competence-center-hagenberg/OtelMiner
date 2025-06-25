import {Bar, BarChart, CartesianGrid, Legend, Tooltip, XAxis, YAxis} from "recharts";
import React, {useMemo} from "react";
import {DeclareConstraint} from "@/app/lib/probDeclare";
import {Box} from "@mui/material";
import Typography from "@mui/material/Typography";

export interface MyValue {
    name: string,
    value: number,
}

export interface StatisticsProps {
    constraints: DeclareConstraint[]
}

function createEmptyThreshholdList() {
    return [
        {name: "[0.0, 0.1[", value: 0},
        {name: "[0.1, 0.2[", value: 0},
        {name: "[0.2, 0.3[", value: 0},
        {name: "[0.3, 0.4[", value: 0},
        {name: "[0.4, 0.5[", value: 0},
        {name: "[0.5, 0.6[", value: 0},
        {name: "[0.6, 0.7[", value: 0},
        {name: "[0.7, 0.8[", value: 0},
        {name: "[0.8, 0.9[", value: 0},
        {name: "[0.9, 1[", value: 0},
        {name: "= 1", value: 0},
    ]
}

const ranges = [
    {min: 0.0, max: 0.1, index: 0},
    {min: 0.1, max: 0.2, index: 1},
    {min: 0.2, max: 0.3, index: 2},
    {min: 0.3, max: 0.4, index: 3},
    {min: 0.4, max: 0.5, index: 4},
    {min: 0.5, max: 0.6, index: 5},
    {min: 0.6, max: 0.7, index: 6},
    {min: 0.7, max: 0.8, index: 7},
    {min: 0.8, max: 0.9, index: 8},
    {min: 0.9, max: 1, index: 9},
];

function mapConstraintsByNrBundled(constraints: DeclareConstraint[]) {
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
    return label.substring(0, maxLength) + '...';
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

const Statistics = ({constraints}: StatisticsProps) => {
    const constraintsByNrBundled = useMemo<MyValue[]>(() => mapConstraintsByNrBundled(constraints), [constraints])
    return (
        <Box>
            <Typography variant="h6">Every Trace by Probability</Typography>
            <BarChart width={1200} height={500} data={constraints}
                      margin={{ top: 20, right: 30, left: 20, bottom: 120 }}
            >
                <CartesianGrid strokeDasharray={"3 3"}/>
                <XAxis
                    dataKey={"declareTemplate"}
                    angle={-45}
                    textAnchor={"end"}
                    height={80}
                    interval={"preserveStartEnd"}
                    tick={<CustomXAxisTick />}
                />
                <YAxis dataKey={"probability"}/>
                <Tooltip content={<CustomTooltip />} />
                <Legend/>
                <Bar dataKey={"probability"} fill={"#8884d8"} name={"Probability"}/>
            </BarChart>
            <Typography variant={"h6"}>Nr by Threshold</Typography>
            <BarChart width={1200} height={250} data={constraintsByNrBundled}>
                <CartesianGrid strokeDasharray={"3 3"}/>
                <XAxis dataKey={"name"}/>
                <YAxis dataKey={"value"}/>
                <Tooltip/>
                <Legend/>
                <Bar dataKey={"value"} fill={"#8884d8"} name={"Count"}/>
            </BarChart>
        </Box>
    );
}

export default Statistics;