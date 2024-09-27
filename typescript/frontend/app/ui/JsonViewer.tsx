import React from 'react';
import { Typography, Box } from '@mui/material';

interface JsonViewerProps {
    data: any;
}

const JsonViewer: React.FC<JsonViewerProps> = ({ data }) => {
    const renderJsonData = (obj: any, level: number = 0) => {
        if (typeof obj === 'object' && obj !== null) {
            return (
                <Box ml={level * 2}>
                    {Object.entries(obj).map(([key, value], index) => (
                        <Box key={index}>
                            <Typography variant="body2" component="span" fontWeight="bold">
                                {key}:
                            </Typography>{' '}
                            {renderJsonData(value, level + 1)}
                        </Box>
                    ))}
                </Box>
            );
        } else {
            return (
                <Typography variant="body2" component="span">
                    {JSON.stringify(obj)}
                </Typography>
            );
        }
    };

    return (
        <Box>
            <Typography variant="h6">JSON Data</Typography>
            {renderJsonData(data)}
        </Box>
    );
};

export default JsonViewer;
