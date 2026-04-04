"use client";

import { useEffect, useState } from "react";

interface TypingTextProps {
  strings: string[];
  typingSpeed?: number;
  pauseTime?: number;
  className?: string;
}

export function TypingText({
  strings,
  typingSpeed = 50,
  pauseTime = 1500,
  className = "",
}: TypingTextProps) {
  const [displayed, setDisplayed] = useState("");
  const [stringIndex, setStringIndex] = useState(0);
  const [charIndex, setCharIndex] = useState(0);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const current = strings[stringIndex];

    const timeout = setTimeout(
      () => {
        if (!isDeleting) {
          setDisplayed(current.slice(0, charIndex + 1));
          setCharIndex((c) => c + 1);

          if (charIndex + 1 === current.length) {
            setTimeout(() => setIsDeleting(true), pauseTime);
          }
        } else {
          setDisplayed(current.slice(0, charIndex - 1));
          setCharIndex((c) => c - 1);

          if (charIndex <= 1) {
            setIsDeleting(false);
            setStringIndex((s) => (s + 1) % strings.length);
            setCharIndex(0);
          }
        }
      },
      isDeleting ? typingSpeed / 2 : typingSpeed
    );

    return () => clearTimeout(timeout);
  }, [charIndex, isDeleting, stringIndex, strings, typingSpeed, pauseTime]);

  return (
    <span className={className}>
      {displayed}
      <span className="typing-cursor" />
    </span>
  );
}
