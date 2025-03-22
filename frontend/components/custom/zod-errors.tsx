export function ZodErrors({ error }: { error: string[] }) {
  if (!error || error.length === 0) return null;

  // Показуємо лише першу помилку
  return (
    <div className="text-pink-500 text-xs italic mt-1 py-2">
      {error[0]}
    </div>
  );
}



// export function ZodErrors({ error }: { error: string[] }) {
//     if (!error) return null;
//     return error.map((err: string, index: number) => (
//       <div key={index} className="text-pink-500 text-xs italic mt-1 py-2">
//         {err}
//       </div>
//     ));
//   }