import * as React from 'react';
import {CssBaseline, ThemeProvider} from "@mui/material";
import theme from "@/app/theme";
import {AppProvider} from '@toolpad/core';

export default function RootLayout(props: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="en" suppressHydrationWarning={true}>
        <body>
            <ThemeProvider theme={theme}>
                <AppProvider>
                    <CssBaseline />
                    {props.children}
                </AppProvider>
            </ThemeProvider>
        </body>
        </html>
    );
}
