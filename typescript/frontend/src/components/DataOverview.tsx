import React, { useEffect, useState } from 'react';
import RestService from '../services/RestService';

interface DataOverviewProps {
    nrNodes: number[];
    nrTraces: number[];
    sourceFile: string;
}

interface TraceDetails {
    traceId: string;
    nrNodes: number;
}

const restService = new RestService('http://localhost:4020/v1/data');

const DataOverview: React.FC = () => {
    const [data, setData] = useState<DataOverviewProps[]>([]);
    const [traceDetails, setTraceDetails] = useState<TraceDetails[] | null>(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await restService.get('/overview');
                setData(response.data);
            } catch (error) {
                console.error('Error fetching data:', error);
            }
        };

        fetchData().catch(error => console.error('Error in fetchData:', error));
    }, []);

    const handleRowClick = async (sourceFile: string) => {
        try {
            const response = await restService.get(`/details?sourceFile=${sourceFile}`);
            setTraceDetails(response.data);
        } catch (error) {
            console.error('Error fetching trace details:', error);
        }
    };

    return (
        <div>
            <table>
                <thead>
                <tr>
                    <th>Nodes</th>
                    <th>Traces</th>
                    <th>Source File</th>
                </tr>
                </thead>
                <tbody>
                {data.map((dataset) => (
                    <tr key={dataset.sourceFile} onClick={() => handleRowClick(dataset.sourceFile)}>
                        <td>{dataset.nrNodes}</td>
                        <td>{dataset.nrTraces}</td>
                        <td>{dataset.sourceFile}</td>
                    </tr>
                ))}
                </tbody>
            </table>

            {traceDetails && (
                <div>
                    <h3>Trace Details</h3>
                    <table>
                        <thead>
                        <tr>
                            <th>Trace ID</th>
                            <th>Number of Nodes</th>
                        </tr>
                        </thead>
                        <tbody>
                        {traceDetails.map((detail) => (
                            <tr key={detail.traceId}>
                                <td>{detail.traceId}</td>
                                <td>{detail.nrNodes}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default DataOverview;