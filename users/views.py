import random

from django.contrib.auth import login
from django.contrib.auth.views import LoginView
from django.core.serializers import serialize
from django.http import JsonResponse
from django.shortcuts import render, redirect
from django.contrib import messages
from django.views import View
from django.views.decorators.csrf import csrf_exempt
from django.db import connection

from .models import Report, JunctionManholeElements, User, Ru, Pozzetti, Tratte
from django.conf import settings
from .forms import RegisterForm, LoginForm, UsersReportForm

from django.contrib.auth.decorators import login_required

from .forms import UpdateUserForm, UpdateProfileForm

from django.urls import reverse_lazy
from django.contrib.auth.views import PasswordChangeView
from django.contrib.messages.views import SuccessMessageMixin

from django.contrib.gis.geos import MultiPoint, Point
from django.urls import reverse_lazy
from django.contrib.auth.views import PasswordResetView
from django.contrib.messages.views import SuccessMessageMixin
from django.contrib.auth.decorators import login_required
import json
from pyproj import Transformer


# Create your views here.
def redirect_if_authenticated(view_func):
    def wrapper(request, *args, **kwargs):
        if request.user.is_authenticated:
            return redirect('/welcome/')  # Replace 'home' with your desired redirect target
        return view_func(request, *args, **kwargs)

    return wrapper


def convert_crs(lat, lng):
    # Create a transformer object
    transformer = Transformer.from_crs("EPSG:4326", "EPSG:3003")
    return transformer.transform(lat, lng)


@redirect_if_authenticated
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

    def dispatch(self, request, *args, **kwargs):
        if request.user.is_authenticated:
            # User is already logged in, redirect to the welcome page
            return redirect('/welcome')
        return super().dispatch(request, *args, **kwargs)

    def form_valid(self, form):
        remember_me = form.cleaned_data.get('remember_me')

        # if not remember_me:
        #     # Set session expiry to 0 seconds to close the session after the browser is closed
        #     self.request.session.set_expiry(0)
        #     self.request.session.modified = True

        if remember_me:
            # Set session expiry to the default (e.g., 2 weeks)
            self.request.session.set_expiry(settings.SESSION_COOKIE_AGE)
        else:
            # Set session expiry to browser close
            self.request.session.set_expiry(0)

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


@login_required
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
    template_name = 'users/registration/password_reset.html'
    email_template_name = 'users/registration/password_reset_email.html'
    subject_template_name = 'users/registration/password_reset_subject.txt'
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


@login_required
def showReports(request):
    if not request.user.is_superuser:
        # If the user is not a superuser, redirect to the welcome page
        return redirect('/welcome')

    # Get distinct years from the Report model
    years = Report.objects.dates('report_date', 'year').distinct()

    # Serialize the reports to GeoJSON format
    reports = Report.objects.all()
    geojson = serialize('geojson', reports, geometry_field='user_coordinate',
                        fields=('report_type', 'description', 'report_date', 'photo', 'user_id'))

    return render(request, 'users/reports.html', {'geojson': geojson, 'years': years})


@login_required
def EnterRegular(request):
    return render(request, 'users/welcome-regular.html')


@csrf_exempt  # Use this if you're not passing CSRF token
def add_point(request):
    if request.method == 'POST':
        try:
            # Parse the incoming data
            data = json.loads(request.body.decode('utf-8'))
            lat = data.get('lat')
            lng = data.get('lng')
            point_type = data.get('point_type')

            # Validate the data
            if lat is None or lng is None or point_type is None:
                return JsonResponse({'error': 'Missing required fields'}, status=400)

            if point_type == "HOME":
                lng, lat = convert_crs(lat, lng)
                point_wkt = f"POINT ZM({lng} {lat} {22.5435} {-100000000000000000000000000000000000000000})"  # SRID 4326 is the standard for lat/lon (WGS84)
                point_id = random.randint(1000, 90000)
                with connection.cursor() as cursor:
                    cursor.execute(
                        "INSERT INTO ru (geom, id) VALUES (ST_GeomFromText(%s, 3003), %s)",
                        [point_wkt, point_id]
                    )
            else:
                # TODO: NEED TO CHANGE TO THE POZZETTI
                lng, lat = convert_crs(lat, lng)
                point_wkt = f"POINT ZM({lng} {lat} {22.5435} {-100000000000000000000000000000000000000000})"  # SRID 4326 is the standard for lat/lon (WGS84)
                point_id = random.randint(1000, 90000)
                with connection.cursor() as cursor:
                    cursor.execute(
                        "INSERT INTO ru (geom, id) VALUES (ST_GeomFromText(%s, 3003), %s)",
                        [point_wkt, point_id]
                    )
            # Save the point to the database (this is where you'd integrate your model)
            # For example:
            # point = Point.objects.create(lat=lat, lng=lng, point_type=point_type)

            # Return success response
            return JsonResponse({'message': 'Point added successfully'}, status=201)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)

    # If not a POST request, return a method not allowed response
    return JsonResponse({'error': 'Invalid request method'}, status=405)



@csrf_exempt  # Use this if you're not passing CSRF token
def connect_points(request):
    if request.method == 'POST':
        try:
            # Parse the incoming data
            data = json.loads(request.body.decode('utf-8'))
            home_id = data.get('point1').split(".")[1]
            hole_id = data.get('point2').split(".")[1]

            print(home_id, hole_id)

            home = Ru.objects.get(gid=home_id).geom
            hole = Pozzetti.objects.get(gid=hole_id).geom

            multilinestring_wkt = (
                f"MULTILINESTRING ZM(({home.x} {home.y} {22.5435} {-100000000000000000000000000000000000000000}, "
                f"{hole.x} {hole.y} {22.5435} {-100000000000000000000000000000000000000000}))"
            )

            line_id = random.randint(1000, 90000)
            with connection.cursor() as cursor:
                cursor.execute(
                    "INSERT INTO tratte (geom, id, diametro, materiale) VALUES (ST_GeomFromText(%s, 3003), %s, %s, %s)",
                    [multilinestring_wkt, line_id, 1, 'PVC']
                )
            # Return success response
            return JsonResponse({'message': 'Point added successfully'}, status=201)

        except json.JSONDecodeError:
            return JsonResponse({'error': 'Invalid JSON'}, status=400)

    # If not a POST request, return a method not allowed response
    return JsonResponse({'error': 'Invalid request method'}, status=405)
