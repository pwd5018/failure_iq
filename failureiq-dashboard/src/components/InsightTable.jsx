function InsightTable({ title, description, columns, rows, emptyMessage, testId }) {
  return (
    <section className="card table-panel" data-testid={testId}>
      <div className="panel-header">
        <div>
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
      </div>

      {rows.length === 0 ? (
        <div className="inline-empty-state">
          <p>{emptyMessage}</p>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                {columns.map((column) => (
                  <th key={column.key}>{column.label}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row, index) => (
                <tr
                  key={row.id || row.testName || `${title}-${index}`}
                  className={row.highlight ? 'failed-row' : ''}
                >
                  {columns.map((column) => (
                    <td key={column.key}>{column.render ? column.render(row) : row[column.key]}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default InsightTable;
