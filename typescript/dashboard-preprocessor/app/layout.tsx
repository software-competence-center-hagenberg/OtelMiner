import * as React from 'react';
import {AppProvider, DashboardLayout, PageContainer} from '@toolpad/core';
import {AppRouterCacheProvider} from '@mui/material-nextjs/v14-appRouter';
import {CssBaseline, ThemeProvider} from "@mui/material";
import theme from "@/app/theme";

export default function RootLayout(props: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="en">
        <body>
          <ThemeProvider theme={theme}>
            {/* CssBaseline kickstart an elegant, consistent, and simple baseline to build upon. */}
            <CssBaseline />
            {props.children}
          </ThemeProvider>
        </body>
        </html>
        // FIXME according to documentation (https://mui.com/material-ui/integrations/nextjs/) better like this,
        //  but layout the layout looks better the current way...
        // <html lang="en">
        // <body>
        // <AppRouterCacheProvider options={{enableCssLayer: true}}>
        //     <AppProvider> {/*navigation={NAVIGATION} branding={BRANDING}*/}
        //         <DashboardLayout>
        //             <PageContainer>
        //                 {props.children}
        //             </PageContainer>
        //         </DashboardLayout>
        //     </AppProvider>
        // </AppRouterCacheProvider>
        // </body>
        // </html>
    );
}