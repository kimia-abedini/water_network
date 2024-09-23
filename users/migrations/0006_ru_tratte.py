# Generated by Django 5.0.6 on 2024-09-22 10:26

import django.contrib.gis.db.models.fields
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('users', '0005_role_profile_role'),
    ]

    operations = [
        migrations.CreateModel(
            name='Ru',
            fields=[
                ('gid', models.AutoField(primary_key=True, serialize=False)),
                ('id', models.FloatField(blank=True, null=True)),
                ('geom', django.contrib.gis.db.models.fields.PointField(srid=4326)),
            ],
            options={
                'db_table': 'ru',
                'managed': False,
            },
        ),
        migrations.CreateModel(
            name='Tratte',
            fields=[
                ('gid', models.AutoField(primary_key=True, serialize=False)),
                ('id', models.FloatField(blank=True, null=True)),
                ('diametro', models.FloatField(blank=True, null=True)),
                ('materiale', models.CharField(max_length=7)),
                ('geom', django.contrib.gis.db.models.fields.PointField(srid=4326)),
            ],
            options={
                'db_table': 'tratte',
                'managed': False,
            },
        ),
    ]