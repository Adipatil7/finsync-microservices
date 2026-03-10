"use client";

import { useAuthStore } from "@/lib/auth-store";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { LogOut, Moon, Sun, Plus } from "lucide-react";
import { useTheme } from "next-themes";
import { useState } from "react";

export function TopNav() {
  const { name, email, logout } = useAuthStore();
  const router = useRouter();
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  // Avoid hydration mismatch
  useState(() => {
    setMounted(true);
  });

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <header className="sticky top-0 z-30 h-16 border-b border-border/50 bg-card/50 backdrop-blur-xl flex items-center justify-between px-6">
      <div>
        <h2 className="text-sm font-medium text-muted-foreground">
          Welcome back,
        </h2>
        <h1 className="text-lg font-semibold">{name || "User"}</h1>
      </div>

      <div className="flex items-center gap-3">
        {mounted && (
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            className="rounded-lg"
          >
            {theme === "dark" ? (
              <Sun className="h-5 w-5" />
            ) : (
              <Moon className="h-5 w-5" />
            )}
          </Button>
        )}

        <div className="flex items-center gap-3 pl-3 border-l border-border/50">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium">{name}</p>
            <p className="text-xs text-muted-foreground">{email}</p>
          </div>
          <div className="w-9 h-9 rounded-full gradient-primary flex items-center justify-center text-white font-semibold text-sm">
            {name?.charAt(0)?.toUpperCase() || "U"}
          </div>
          <Button variant="ghost" size="icon" onClick={handleLogout} className="rounded-lg text-muted-foreground hover:text-destructive">
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </header>
  );
}
