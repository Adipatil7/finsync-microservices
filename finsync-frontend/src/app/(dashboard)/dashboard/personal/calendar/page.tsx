"use client";

import { useState, useMemo, useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { ChevronLeft, ChevronRight, X } from "lucide-react";
import { format, startOfMonth, endOfMonth, eachDayOfInterval, getDay, addMonths, subMonths, isSameMonth, isToday, parseISO } from "date-fns";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { personalService } from "@/services/personal-service";
import { formatCurrency } from "@/lib/utils";
import type { CalendarDayExpenseResponse, TransactionResponse } from "@/types";

export default function CalendarPage() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDay, setSelectedDay] = useState<CalendarDayExpenseResponse | null>(null);

  const monthStr = format(currentDate, "yyyy-MM");

  const { data: calendarData } = useQuery({
    queryKey: ["calendar", monthStr],
    queryFn: () => personalService.getCalendarExpenses(monthStr),
  });

  // Build lookup: date string -> CalendarDayExpenseResponse
  const dayLookup = useMemo(() => {
    const map: Record<string, CalendarDayExpenseResponse> = {};
    calendarData?.forEach((d) => {
      map[d.date] = d;
    });
    return map;
  }, [calendarData]);

  // Calendar grid
  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  const daysInMonth = eachDayOfInterval({ start: monthStart, end: monthEnd });
  const startDayOfWeek = getDay(monthStart); // 0=Sun

  const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">Calendar</h1>
        <p className="text-muted-foreground mt-1">View your daily spending at a glance</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Calendar Grid */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between">
            <Button variant="ghost" size="icon" onClick={() => setCurrentDate(subMonths(currentDate, 1))}>
              <ChevronLeft className="w-5 h-5" />
            </Button>
            <CardTitle className="text-xl">{format(currentDate, "MMMM yyyy")}</CardTitle>
            <Button variant="ghost" size="icon" onClick={() => setCurrentDate(addMonths(currentDate, 1))}>
              <ChevronRight className="w-5 h-5" />
            </Button>
          </CardHeader>
          <CardContent>
            {/* Day headers */}
            <div className="grid grid-cols-7 gap-1 mb-2">
              {weekDays.map((day) => (
                <div key={day} className="text-center text-xs font-semibold text-muted-foreground py-2">
                  {day}
                </div>
              ))}
            </div>

            {/* Day cells */}
            <div className="grid grid-cols-7 gap-1">
              {/* Empty cells for offset */}
              {Array.from({ length: startDayOfWeek }).map((_, i) => (
                <div key={`empty-${i}`} className="aspect-square" />
              ))}
              {daysInMonth.map((day) => {
                const dateStr = format(day, "yyyy-MM-dd");
                const dayData = dayLookup[dateStr];
                const hasExpenses = dayData && dayData.totalExpenseAmount > 0;
                const today = isToday(day);

                return (
                  <button
                    key={dateStr}
                    onClick={() => dayData && setSelectedDay(dayData)}
                    className={`aspect-square p-1 rounded-lg border text-left flex flex-col transition-all duration-200 hover:border-primary/50 hover:bg-accent/50 ${
                      today ? "border-primary bg-primary/5" : "border-border/30"
                    } ${selectedDay?.date === dateStr ? "ring-2 ring-primary" : ""}`}
                  >
                    <span className={`text-sm font-medium ${today ? "text-primary" : ""}`}>
                      {format(day, "d")}
                    </span>
                    {hasExpenses && (
                      <div className="mt-auto">
                        <p className="text-xs font-semibold text-rose-500 truncate">
                          {formatCurrency(dayData.totalExpenseAmount)}
                        </p>
                        {dayData.expenses?.length > 0 && (
                          <div className="flex gap-0.5 mt-0.5 flex-wrap">
                            {dayData.expenses.slice(0, 3).map((e, i) => (
                              <span
                                key={i}
                                className="text-[9px] bg-accent px-1 rounded truncate max-w-full"
                              >
                                {e.categoryName}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </CardContent>
        </Card>

        {/* Day Details Panel */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg">
              {selectedDay ? format(parseISO(selectedDay.date), "MMM d, yyyy") : "Select a day"}
            </CardTitle>
            {selectedDay && (
              <Button variant="ghost" size="icon" onClick={() => setSelectedDay(null)}>
                <X className="w-4 h-4" />
              </Button>
            )}
          </CardHeader>
          <CardContent>
            {selectedDay ? (
              <div className="space-y-4">
                <div className="p-3 bg-rose-500/10 rounded-lg">
                  <p className="text-sm text-muted-foreground">Total Spent</p>
                  <p className="text-xl font-bold text-rose-500">
                    {formatCurrency(selectedDay.totalExpenseAmount)}
                  </p>
                </div>
                <div className="space-y-2">
                  {selectedDay.expenses?.map((tx) => (
                    <div
                      key={tx.id}
                      className="p-3 rounded-lg border border-border/50 hover:bg-accent/30 transition-colors"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-medium text-sm">{tx.description || tx.categoryName}</p>
                          <span className="text-xs px-2 py-0.5 bg-accent rounded-md mt-1 inline-block">
                            {tx.categoryName}
                          </span>
                        </div>
                        <p className="font-semibold text-rose-500">
                          {formatCurrency(tx.amount)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="py-8 text-center text-muted-foreground">
                <p className="text-sm">Click a day on the calendar to view transactions</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </motion.div>
  );
}
