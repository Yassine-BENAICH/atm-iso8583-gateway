from __future__ import annotations

import html
import sys
from pathlib import Path

import markdown


EDGE_CANDIDATES = [
    Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"),
    Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"),
]

METADATA = {
    "title": "Conception et developpement d'une passerelle ISO 8583",
    "student": "YASSINE BENAICH",
    "school": "ENSET Mohammedia",
    "program": "Big Data and Cloud Computing",
    "company": "Hightech Payment Systems",
    "location": "Casablanca",
    "period": "Du 13 janvier 2026 au 12 juin 2026",
    "academic_supervisor": "MOHAMED YOUSSFI",
    "professional_supervisor": "OTHMAN KHOULFY",
    "academic_year": "2025 / 2026",
}


def find_edge() -> Path:
    for candidate in EDGE_CANDIDATES:
        if candidate.exists():
            return candidate
    raise FileNotFoundError("Microsoft Edge introuvable.")


def strip_frontmatter_sections(text: str) -> str:
    lines = text.splitlines()
    out: list[str] = []
    skipping = False

    for line in lines:
        if line.strip() == "## Titre":
            skipping = True
            continue
        if skipping and line.startswith("## Remerciements"):
            skipping = False
            out.append(line)
            continue
        if not skipping:
            out.append(line)

    return "\n".join(out).strip() + "\n"


def build_cover_page() -> str:
    meta = {k: html.escape(v) for k, v in METADATA.items()}
    return f"""
    <section class="cover-page">
      <div class="cover-topbar"></div>
      <div class="cover-body">
        <p class="cover-country">Royaume du Maroc</p>
        <p class="cover-ministry">Ministere de l'Enseignement Superieur, de la Recherche Scientifique et de l'Innovation</p>
        <p class="cover-school">{meta['school']}</p>
        <p class="cover-program">{meta['program']}</p>
        <p class="cover-kind">Rapport de Projet de Fin d'Etudes</p>
        <h1>{meta['title']}</h1>
        <div class="cover-grid">
          <div>
            <span class="label">Realise par</span>
            <strong>{meta['student']}</strong>
          </div>
          <div>
            <span class="label">Entreprise d'accueil</span>
            <strong>{meta['company']}</strong>
          </div>
          <div>
            <span class="label">Lieu</span>
            <strong>{meta['location']}</strong>
          </div>
          <div>
            <span class="label">Periode</span>
            <strong>{meta['period']}</strong>
          </div>
          <div>
            <span class="label">Encadrant academique</span>
            <strong>{meta['academic_supervisor']}</strong>
          </div>
          <div>
            <span class="label">Encadrant professionnel</span>
            <strong>{meta['professional_supervisor']}</strong>
          </div>
        </div>
        <p class="cover-year">Annee universitaire : {meta['academic_year']}</p>
      </div>
      <div class="cover-bottombar"></div>
    </section>
    """


def build_html(markdown_text: str, css_href: str) -> str:
    clean_text = strip_frontmatter_sections(markdown_text)
    md = markdown.Markdown(
        extensions=["toc", "tables", "fenced_code", "sane_lists", "nl2br"],
        extension_configs={"toc": {"permalink": False}},
    )
    body = md.convert(clean_text)
    toc = md.toc

    return f"""<!DOCTYPE html>
<html lang="fr">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>{html.escape(METADATA['title'])}</title>
    <link rel="stylesheet" href="{css_href}" />
  </head>
  <body>
    {build_cover_page()}
    <section class="toc-page">
      <h2>Sommaire</h2>
      <div class="toc-card">
        {toc}
      </div>
    </section>
    <main class="report-body">
      {body}
    </main>
  </body>
</html>
"""


def main() -> int:
    if len(sys.argv) != 4:
        print("Usage: python render_pfe_pdf.py <input.md> <output.html> <output.pdf>")
        return 1

    input_md = Path(sys.argv[1]).resolve()
    output_html = Path(sys.argv[2]).resolve()
    output_pdf = Path(sys.argv[3]).resolve()
    css_file = (Path(__file__).resolve().parent / "pfe-report.css").resolve()

    text = input_md.read_text(encoding="utf-8")
    html_text = build_html(text, css_file.as_uri())
    output_html.write_text(html_text, encoding="utf-8")

    edge = find_edge()
    cmd = [
        str(edge),
        "--headless=new",
        "--disable-gpu",
        "--run-all-compositor-stages-before-draw",
        "--print-to-pdf-no-header",
        f"--print-to-pdf={output_pdf}",
        output_html.as_uri(),
    ]

    import subprocess

    completed = subprocess.run(cmd, check=False, capture_output=True, text=True)
    if completed.returncode != 0:
        print(completed.stdout)
        print(completed.stderr)
        return completed.returncode

    print(f"HTML generated: {output_html}")
    print(f"PDF generated: {output_pdf}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
