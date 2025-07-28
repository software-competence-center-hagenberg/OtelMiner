import * as React from 'react';
import DataOverview from "@/app/ui/DataOverview";
import {DashboardLayout} from '@toolpad/core';

const Page: React.FC = () => {
    return (
        <DashboardLayout title={"Data Explorer"}>
            <DataOverview/>
        </DashboardLayout>
    );
}

export default Page;