import React, { Component } from 'react';
import { Typography, Box } from '@mui/material';
import './JsonView.css';

interface JsonViewProps {
    data: any;
}

class JsonView extends Component<JsonViewProps> {
    renderJsonData = (obj: any, indent: number = 0) => {
        if (typeof obj === 'object' && obj !== null) {
            return (
                <Box className={`indent-${indent}`}>
                    {Object.entries(obj).map(([key, value]) => (
                        <Box key={`${key}-${JSON.stringify(value)}`}> {/* Use a combination of key and value for uniqueness */}
                            <Typography variant="body2" component="span" fontWeight="bold">
                                {key}:
                            </Typography>{' '}
                            {this.renderJsonData(value, indent + 1)}
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
    }

    render = () => {
        const { data } = this.props;
        return (

            <Box className="json-viewer">
                <Typography variant="h6">JSON Data</Typography>
                {this.renderJsonData(data)}
            </Box>
        );
    }
}

export default JsonView;
