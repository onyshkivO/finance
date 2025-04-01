"use client"

import * as React from "react"
import * as ProgressPrimitive from "@radix-ui/react-progress"
import { cn } from "@/lib/utils"

interface ProgressProps extends React.ComponentProps<typeof ProgressPrimitive.Root> {
  indicator?: string;
}

const Progress = React.forwardRef<
  React.ElementRef<typeof ProgressPrimitive.Root>,
  ProgressProps
>(({ className, value, indicator, ...props }, ref) => {
  return (
    <ProgressPrimitive.Root
      ref={ref}
      data-slot="progress"
      className={cn(
        "bg-primary/20 relative h-4 w-full overflow-hidden rounded-full",
        className
      )}
      {...props}
    >
      <ProgressPrimitive.Indicator
        data-slot="progress-indicator"
        className={cn(
          "bg-primary h-full w-full flex-1 transition-all",
          indicator
        )}
        style={{ transform: `translateX(-${100 - (value || 0)}%)` }}
      />
    </ProgressPrimitive.Root>
  )
})

Progress.displayName = ProgressPrimitive.Root.displayName

export { Progress }