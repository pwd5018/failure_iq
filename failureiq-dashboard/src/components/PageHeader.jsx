function PageHeader({ eyebrow, title, subtitle, action }) {
  return (
    <div className="page-header">
      <div>
        <p className="section-kicker">{eyebrow}</p>
        <h2>{title}</h2>
        <p className="page-subtitle">{subtitle}</p>
      </div>
      {action ? <div>{action}</div> : null}
    </div>
  );
}

export default PageHeader;
