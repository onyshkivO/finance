import React, { ReactNode } from 'react'
import Navbar from "@/components/custom/Navbar";

function layout({children}:{children:ReactNode}) {
  return (
    <div className='relative flex h-screen w-full flex-col'>
        <Navbar/>
          <div className='w-full'>{children}</div>
    </div>
  )
}

export default layout