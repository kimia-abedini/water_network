from django.contrib.auth import login
from django.contrib.auth.views import LoginView
from django.core.serializers import serialize
from django.shortcuts import render, redirect
from django.contrib import messages
from django.views import View
from .models import Report, JunctionManholeElements, User

from .forms import RegisterForm, LoginForm, UsersReportForm

from django.contrib.auth.decorators import login_required

from .forms import UpdateUserForm, UpdateProfileForm

from django.urls import reverse_lazy
from django.contrib.auth.views import PasswordChangeView
from django.contrib.messages.views import SuccessMessageMixin

from django.contrib.gis.geos import Point
from django.urls import reverse_lazy
from django.contrib.auth.views import PasswordResetView
from django.contrib.messages.views import SuccessMessageMixin
from django.contrib.auth.decorators import login_required
from django.views.decorators.http import require_POST
import json


# Create your views here.

def home(request):
    return render(request, 'users/home.html')


class RegisterView(View):
    form_class = RegisterForm
    initial = {'key': 'value'}
    template_name = 'users/register.html'

    def dispatch(self, request, *args, **kwargs):
        # will redirect to the home page if a user tries to access the register page while logged in
        if request.user.is_authenticated:
            return redirect(to='/')

        # else process dispatch as it otherwise normally would
        return super(RegisterView, self).dispatch(request, *args, **kwargs)

    def get(self, request, *args, **kwargs):
        form = self.form_class(initial=self.initial)
        return render(request, self.template_name, {'form': form})

    def post(self, request, *args, **kwargs):
        form = self.form_class(request.POST)

        if form.is_valid():
            form.save()

            username = form.cleaned_data.get('username')
            messages.success(request, f'Account created for {username}')

            return redirect(to='/')

        return render(request, self.template_name, {'form': form})


# class CustomLoginView(LoginView):
#     form_class = LoginForm
#
#     def form_valid(self, form):
#         remember_me = form.cleaned_data.get('remember_me')
#
#         if not remember_me:
#             # set session expiry to 0 seconds. So it will automatically close the session after the browser is closed.
#             self.request.session.set_expiry(0)
#
#             # Set session as modified to force data updates/cookie to be saved.
#             self.request.session.modified = True
#
#
#
#
#         # else browser session will be as long as the session cookie time "SESSION_COOKIE_AGE" defined in settings.py
#         return super(CustomLoginView, self).form_valid(form)






class CustomLoginView(LoginView):
    form_class = LoginForm

    def form_valid(self, form):
        remember_me = form.cleaned_data.get('remember_me')

        if not remember_me:
            # Set session expiry to 0 seconds to close the session after the browser is closed
            self.request.session.set_expiry(0)
            self.request.session.modified = True

        # Log the user in
        login(self.request, form.get_user())

        # Redirect based on user type
        if self.request.user.is_superuser:
            return redirect('admin:index')  # Redirect to the admin panel
        else:
            return redirect('/welcome')  # Redirect to the regular user page

    def get_success_url(self):
        # Fallback URL in case something goes wrong with the redirection in form_valid
        return reverse_lazy('home')


def regularUser(request):
    years = Report.objects.dates('report_date', 'year').distinct()

    # Serialize the reports to GeoJSON format
    elements = {}
    all_junction_elements = JunctionManholeElements.objects.all()
    for alje in all_junction_elements:
        po_id = alje.pozzetti_id
        if po_id in elements:
            elements[po_id].append({"element_type": alje.element_type, "installed_date": str(alje.installation_date)})
        else:
            elements[po_id] = [{"element_type": alje.element_type, "installed_date": str(alje.installation_date)}]

    return render(request, 'users/regular.html', {"elements": elements})


class ResetPasswordView(SuccessMessageMixin, PasswordResetView):
    template_name = 'users/password_reset.html'
    email_template_name = 'users/password_reset_email.html'
    subject_template_name = 'users/password_reset_subject'
    success_message = "We've emailed you instructions for setting your password, " \
                      "if an account exists with the email you entered. You should receive them shortly." \
                      " If you don't receive an email, " \
                      "please make sure you've entered the address you registered with, and check your spam folder."
    success_url = reverse_lazy('users-home')


@login_required
def profile(request):
    if request.method == 'POST':
        user_form = UpdateUserForm(request.POST, instance=request.user)
        profile_form = UpdateProfileForm(request.POST, request.FILES, instance=request.user.profile)

        if user_form.is_valid() and profile_form.is_valid():
            user_form.save()
            profile_form.save()
            messages.success(request, 'Your profile is updated successfully')
            return redirect(to='users-profile')
    else:
        user_form = UpdateUserForm(instance=request.user)
        profile_form = UpdateProfileForm(instance=request.user.profile)

    return render(request, 'users/profile.html', {'user_form': user_form, 'profile_form': profile_form})


class ChangePasswordView(SuccessMessageMixin, PasswordChangeView):
    template_name = 'users/change_password.html'
    success_message = "Successfully Changed Your Password"
    success_url = reverse_lazy('users-home')


########################################
@login_required
def submit_report(request):
    if request.method == 'POST':
        form = UsersReportForm(request.POST, request.FILES)
        if form.is_valid():
            users_report = form.save(commit=False)
            users_report.user_id = request.user  # Assign the logged-in user to user_id field

            # Process the location field
            location = request.POST.get('location')
            if location:
                lng, lat = map(float, location[6:-1].split())
                point = Point(lng, lat)
                users_report.user_coordinate = point

            users_report.save()
            return redirect('success_page')  # Redirect to a success page or report list
    else:
        form = UsersReportForm()
    return render(request, 'users/regular.html', {'form': form})


def success_page(request):
    return render(request, 'users/success.html')


def showReports(request):
    # Get distinct years from the Report model
    years = Report.objects.dates('report_date', 'year').distinct()

    # Serialize the reports to GeoJSON format
    reports = Report.objects.all()
    geojson = serialize('geojson', reports, geometry_field='user_coordinate',
                        fields=('report_type', 'description', 'report_date', 'photo', 'user_id'))

    return render(request, 'users/reports.html', {'geojson': geojson, 'years': years})


def EnterRegular(request):
    return render(request, 'users/welcome-regular.html')