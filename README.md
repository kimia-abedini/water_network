# 🌊 Water Network Management System

<div align="center">

![Water Network](https://img.shields.io/badge/GIS-Water%20Network-blue?style=for-the-badge&logo=qgis)
![Python](https://img.shields.io/badge/Python-3.8+-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Django](https://img.shields.io/badge/Django-092E20?style=for-the-badge&logo=django&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

*A comprehensive GIS-based solution for efficient water network infrastructure management*

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Documentation](#-documentation) • [Contributing](#-contributing)

</div>

---

## 📖 Overview

Water Network Management System is a powerful Geographic Information System (GIS) application designed to streamline the management, monitoring, and analysis of water distribution networks. Built with Django and integrated GIS capabilities, this system provides water utilities and municipalities with tools to efficiently manage their infrastructure, optimize operations, and generate detailed reports.

### Why This Project?

Managing water distribution networks is a complex task that requires:
- **Spatial awareness** of pipes, valves, and infrastructure
- **Real-time monitoring** of network performance
- **Efficient data management** for maintenance and planning
- **Analytical tools** for decision-making

This system addresses these challenges by combining the power of GIS technology with modern web applications.

---

## ✨ Features

### 🗺️ GIS Integration
- **Interactive mapping** of water network infrastructure
- **Spatial analysis** tools for network optimization
- **Layer management** for different infrastructure components
- **Geospatial data visualization** and editing

### 📊 Network Management
- **Infrastructure tracking** (pipes, valves, pumps, tanks)
- **Asset management** and maintenance scheduling
- **Network topology** analysis
- **Leak detection** and monitoring

### 👥 User Management
- **Role-based access control** (Administrators, Operators, Viewers)
- **User authentication** and authorization
- **Activity logging** and audit trails
- **Customizable user permissions**

### 📈 Reporting & Analytics
- **Automated report generation** with spatial data
- **Performance metrics** and KPI tracking
- **Data visualization** through charts and maps
- **Export capabilities** (PDF, Excel, GIS formats)

### 🔌 Plugin Architecture
- **Extensible plugin system** for custom functionality
- **Integration with QGIS** or other GIS platforms
- **API endpoints** for third-party integrations
- **Custom tool development** support

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Python, Django Framework |
| **Frontend** | HTML5, JavaScript, CSS3 |
| **GIS Engine** | GeoDjango, PostGIS |
| **Database** | PostgreSQL with PostGIS extension |
| **Mapping** | Leaflet / OpenLayers |
| **Plugin Development** | Java, Python |

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Python** 3.8 or higher
- **PostgreSQL** 12+ with PostGIS extension
- **GDAL** (Geospatial Data Abstraction Library)
- **Git** for version control
- **pip** for Python package management

### System Dependencies

#### Ubuntu/Debian:
```bash
sudo apt-get update
sudo apt-get install python3-pip python3-dev libpq-dev postgresql postgresql-contrib postgis gdal-bin libgdal-dev
```

#### macOS (using Homebrew):
```bash
brew install postgresql postgis gdal
```

#### Windows:
- Install PostgreSQL from [official website](https://www.postgresql.org/download/windows/)
- Install PostGIS during PostgreSQL installation
- Install OSGeo4W for GDAL support

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/kimia-abedini/water_network.git
cd water_network
```

### 2. Create Virtual Environment

```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate
```

### 3. Install Dependencies

```bash
pip install -r requirements.txt
```

### 4. Database Setup

Create a PostgreSQL database with PostGIS extension:

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE water_network_db;

-- Connect to the database
\c water_network_db

-- Enable PostGIS extension
CREATE EXTENSION postgis;
```

### 5. Configure Environment Variables

Create a `.env` file in the project root:

```env
DEBUG=True
SECRET_KEY=your-secret-key-here
DATABASE_NAME=water_network_db
DATABASE_USER=postgres
DATABASE_PASSWORD=your-password
DATABASE_HOST=localhost
DATABASE_PORT=5432
ALLOWED_HOSTS=localhost,127.0.0.1
```

### 6. Run Migrations

```bash
python manage.py migrate
```

### 7. Create Superuser

```bash
python manage.py createsuperuser
```

### 8. Load Initial Data (Optional)

```bash
python manage.py loaddata initial_data.json
```

### 9. Collect Static Files

```bash
python manage.py collectstatic
```

### 10. Run Development Server

```bash
python manage.py runserver
```

Visit `http://localhost:8000` in your web browser.

---

## 📱 Usage

### Admin Interface

1. Navigate to `http://localhost:8000/admin`
2. Log in with your superuser credentials
3. Manage users, infrastructure data, and system settings

### Main Application

1. Access the main interface at `http://localhost:8000`
2. Use the interactive map to view and manage water network infrastructure
3. Create, edit, and delete network components
4. Generate reports and perform spatial analysis

### Common Workflows

#### Adding New Infrastructure
1. Navigate to the map interface
2. Select the infrastructure type (pipe, valve, etc.)
3. Click on the map to place the new component
4. Fill in the attribute form
5. Save the changes

#### Generating Reports
1. Go to Reports section
2. Select report type and parameters
3. Choose date range and filters
4. Generate and download the report

#### User Management
1. Access Admin panel → Users
2. Add new user with appropriate role
3. Assign permissions based on responsibilities
4. User receives credentials via email

---

## 📁 Project Structure

```
water_network/
├── gisProject/              # Main Django project settings
│   ├── settings.py         # Project configuration
│   ├── urls.py             # URL routing
│   └── wsgi.py             # WSGI configuration
├── Plugin/                  # Plugin system for extensions
│   └── ...                 # Custom plugins
├── users/                   # User management app
│   ├── models.py           # User models
│   ├── views.py            # User views
│   └── templates/          # User templates
├── templates/               # HTML templates
│   └── admin/              # Custom admin templates
├── media/                   # User-uploaded files
│   └── report_images/      # Generated report images
├── static/                  # Static files (CSS, JS, images)
├── manage.py               # Django management script
├── requirements.txt        # Python dependencies
└── README.md               # This file
```

---

## 🔌 Plugin Development

The system supports custom plugins for extended functionality.

### Creating a Plugin

1. Create a new directory in the `Plugin/` folder
2. Implement the plugin interface:

```python
from plugin_base import BasePlugin

class MyWaterPlugin(BasePlugin):
    def __init__(self):
        self.name = "My Water Plugin"
        self.version = "1.0.0"
    
    def execute(self, context):
        # Your plugin logic here
        pass
```

3. Register the plugin in `Plugin/__init__.py`

---

## 🧪 Testing

Run the test suite:

```bash
# Run all tests
python manage.py test

# Run specific app tests
python manage.py test users

# Run with coverage
coverage run --source='.' manage.py test
coverage report
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

### Coding Standards

- Follow PEP 8 for Python code
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Comment complex logic

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **GeoDjango** for providing excellent GIS capabilities
- **OpenStreetMap** for base map data
- **PostgreSQL** and **PostGIS** for spatial database functionality
- All contributors who have helped shape this project

---

## 📞 Contact & Support

- **Project Maintainer**: [Kimia Abedini](https://github.com/kimia-abedini)
- **Issues**: [GitHub Issues](https://github.com/kimia-abedini/water_network/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kimia-abedini/water_network/discussions)

---

## 🗺️ Roadmap

### Version 2.0 (Planned)
- [ ] Mobile application support
- [ ] Real-time sensor data integration
- [ ] Advanced hydraulic modeling
- [ ] Machine learning for predictive maintenance
- [ ] Multi-language support
- [ ] API documentation with Swagger
- [ ] Docker containerization
- [ ] Cloud deployment guides

---

## 💡 Tips & Best Practices

### Performance Optimization
- Use spatial indexes for large datasets
- Implement caching for frequently accessed data
- Optimize database queries with `select_related()` and `prefetch_related()`

### Security
- Always use HTTPS in production
- Regularly update dependencies
- Implement rate limiting for API endpoints
- Use environment variables for sensitive data

### Data Management
- Regular database backups
- Data validation before import
- Use transactions for critical operations
- Maintain data quality standards

---

<div align="center">

### ⭐ Star this repository if you find it helpful!

**Built with ❤️ for better water management**

</div>
