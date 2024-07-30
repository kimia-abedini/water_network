from django.contrib import admin
from django.contrib.admin import DateFieldListFilter
from django.urls import reverse
from django.utils.html import format_html
from django.utils.safestring import mark_safe
from leaflet.admin import LeafletGeoAdmin
from .models import Report, Profile
from .forms import ReportAdminForm
from django.contrib.gis.admin import GISModelAdmin

from django.contrib.admin import SimpleListFilter
from django.utils.translation import gettext_lazy as _

admin.site.register(Profile)

class YearListFilter(SimpleListFilter):
    title = _('year')
    parameter_name = 'year'

    def lookups(self, request, model_admin):
        # Get distinct years from the report_date field
        years = Report.objects.dates('report_date', 'year')
        return [(year.year, year.year) for year in years]

    def queryset(self, request, queryset):
        # Filter by the selected year
        if self.value():
            return queryset.filter(report_date__year=self.value())
        return queryset


@admin.register(Report)
class UsersReportAdmin(LeafletGeoAdmin):
    # change_list_template = "admin/change_list.html"
    form = ReportAdminForm
    list_display = ('report_type', 'user_coordinate', 'description', 'report_date')
    readonly_fields = ('display_photo', 'report_date',)
    ordering = ('-report_date',)  # Sort by report_date descending
    list_filter = (YearListFilter, 'report_type')  # Include the custom year filter and any other filters

    def display_photo(self, obj):
        if obj.photo:
            return mark_safe(f'<img src="{obj.photo.url}" width="200" />')
        else:
            return 'No photo available'

    def get_form(self, request, obj=None, **kwargs):
        # Customize form depending on whether it's for adding or changing an object
        if obj:
            self.form.base_fields['report_date'].disabled = True
        return super().get_form(request, obj, **kwargs)
