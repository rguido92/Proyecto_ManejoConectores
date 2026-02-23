# Python Learning Project

Pequeño proyecto de prácticas para aprender Python.

Requisitos:
- Python 3.10+ (recomendado)

Instalación y uso (Windows PowerShell):

```powershell
cd python-learning-project
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m src.main
```

Ejecutar tests:

```powershell
.\.venv\Scripts\Activate.ps1
pytest -q
```

Qué contiene:
- `src/` : código fuente
- `tests/` : pruebas con `pytest`
- `requirements.txt` : dependencias de desarrollo

Siguientes pasos sugeridos:
- Añadir más ejercicios, notebooks y retos
- Integrar linter (`flake8`) y formateador (`black`)
- Añadir CI (GitHub Actions) para ejecutar tests automáticamente
