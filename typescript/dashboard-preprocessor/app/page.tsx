import * as React from 'react';
import DataOverview from "@/app/ui/DataOverview";
import {AppProvider, DashboardLayout} from '@toolpad/core';

const Page: React.FC = () => {
    return (
        <AppProvider>
            <DashboardLayout title={"Data Explorer"}>
                    <DataOverview/>
            </DashboardLayout>
        </AppProvider>
    );
}

export default Page;