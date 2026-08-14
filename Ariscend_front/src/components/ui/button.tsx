import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import type { ButtonHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

const buttonVariants = cva(
  "focus-ring inline-flex cursor-pointer items-center justify-center gap-2 rounded-full font-semibold transition disabled:pointer-events-none disabled:opacity-40",
  {
    variants: {
      variant: {
        primary: "bg-white text-black hover:bg-neutral-200",
        secondary: "border border-white/15 bg-white/[0.03] text-white hover:bg-white/[0.08]",
        ghost: "text-[#a9abad] hover:bg-white/[0.06] hover:text-white",
        danger: "border border-red-300/20 bg-red-950/30 text-red-200 hover:bg-red-950/50",
      },
      size: {
        default: "h-11 px-5 text-sm",
        small: "min-h-11 px-4 text-xs md:min-h-9",
        icon: "size-11 p-0",
      },
    },
    defaultVariants: { variant: "primary", size: "default" },
  },
);

interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

export function Button({ className, variant, size, asChild, ...props }: ButtonProps) {
  const Component = asChild ? Slot : "button";
  return <Component className={cn(buttonVariants({ variant, size }), className)} {...props} />;
}
