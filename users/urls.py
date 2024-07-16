from django.urls import path
from .views import home, showReports, RegisterView, LoginView, regularUser, profile, submit_report, success_page
from django.contrib import admin

urlpatterns = [
    path('', home, name='users-home'),
    path('register/', RegisterView.as_view(), name='users-register'),
    # path('login/',LoginView.as_view(),name='login')
    path('regular/',regularUser,name='regular-user'),
    path('profile/', profile, name='users-profile'),
    path('submit_report/', submit_report, name='submit_report'),
    path('success/', success_page, name='success_page'),
    path('reports/',showReports,name='reports')
]