"use client"

import * as React from 'react';
import { CssBaseline, ThemeProvider } from "@mui/material";
import theme from "@/app/theme";
import { AppProvider } from '@toolpad/core';
import { CacheProvider } from '@emotion/react';
import createEmotionCache from '@/app/createEmotionCache';

// Client-side cache, shared for the whole session of the user in the browser
const clientSideEmotionCache = createEmotionCache();

export default function RootLayout(props: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="en">
        <body>
        <CacheProvider value={clientSideEmotionCache}>
            <AppProvider>
                <ThemeProvider theme={theme}>
                    <CssBaseline />
                    {props.children}
                </ThemeProvider>
            </AppProvider>
        </CacheProvider>
        </body>
        </html>
    );
}